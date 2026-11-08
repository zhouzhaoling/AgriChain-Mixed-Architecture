from fastapi import FastAPI
from pydantic import BaseModel
from langchain_ollama import ChatOllama

app = FastAPI(title="AgriChain AI Reflection & Routing Microservice")

# 连接本地大模型 (强制 temperature=0 保证逻辑稳定)
llm = ChatOllama(model="qwen2.5:7b", temperature=0)

# ==========================================
# 1. 反思提炼接口 (AI Loop)
# ==========================================
class ReflectRequest(BaseModel):
    user_query: str
    wrong_skill: str
    correct_skill: str
    tenant_id: str

@app.post("/api/reflect")
async def reflect(req: ReflectRequest):
    print(f"\n🐍 [Python 微服务] 收到反思请求")
    print(f"租户: {req.tenant_id}")
    print(f"用户输入: {req.user_query}")
    print(f"错误路由: {req.wrong_skill} -> 正确路由: {req.correct_skill}")
    
    prompt = f"""
    业务场景：用户输入 '{req.user_query}'
    系统错误地使用了 {req.wrong_skill}，但专家指出应该使用 {req.correct_skill}。
    请总结出一条简短的【避坑规则】，格式为：“当用户提到XXX时，务必路由到XXX”。
    只输出规则本身，不要标点。
    """
    
    print("🧠 正在调用大模型进行反思提炼...")
    rule = llm.invoke(prompt).content.strip()
    print(f"✅ 反思完成，生成规则: {rule}")
    
    return {"status": "success", "new_rule": rule}


# ==========================================
# 2. 路由决策接口 (Java 主干调用的)
# ==========================================
class RouteRequest(BaseModel):
    user_query: str
    skills_info: str  # Java 端拼装好的 Prompt (包含了历史避坑规则)

@app.post("/api/route")
async def route_intent(req: RouteRequest):
    print(f"\n🐍 [Python 路由服务] 收到决策请求: {req.user_query}")
    
    # Java 已经把 Prompt 和规则拼好了，我们直接在这个基础上加用户输入发给大模型即可
    prompt = f"""
    {req.skills_info}
    
    【用户输入】
    {req.user_query}
    
    要求：只输出选中的 Skill 名称，不要输出任何标点或其他文字。
    """
    
    print("🧠 正在调用大模型进行意图路由...")
    intent = llm.invoke(prompt).content.strip()
    print(f"✅ [Python 路由服务] 决策结果: {intent}")
    
    return {"intent": intent}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)