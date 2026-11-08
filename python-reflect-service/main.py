from fastapi import FastAPI
from pydantic import BaseModel
from langchain_ollama import ChatOllama

app = FastAPI(title="AgriChain AI Reflection Microservice")

# 连接本地大模型 (强制 temperature=0 保证反思逻辑稳定)
llm = ChatOllama(model="qwen2.5:7b", temperature=0)

# 定义 Java 传过来的数据结构（强类型校验）
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
    
    # 构造反思 Prompt
    prompt = f"""
    业务场景：用户输入 '{req.user_query}'
    系统错误地使用了 {req.wrong_skill}，但专家指出应该使用 {req.correct_skill}。
    请总结出一条简短的【避坑规则】，格式为：“当用户提到XXX时，务必路由到XXX”。
    只输出规则本身，不要标点。
    """
    
    print("🧠 正在调用大模型进行反思提炼...")
    # 真实调用大模型
    rule = llm.invoke(prompt).content.strip()
    print(f"✅ 反思完成，生成规则: {rule}")
    
    return {"status": "success", "new_rule": rule}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)