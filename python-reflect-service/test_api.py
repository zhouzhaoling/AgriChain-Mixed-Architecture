import requests
import json

url = "http://127.0.0.1:8000/api/reflect"
payload = {
    "user_query": "老张说饲料发霉了",
    "wrong_skill": "sales_analysis",
    "correct_skill": "traceability",
    "tenant_id": "dabeinong"
}

print(">>> 正在发送测试请求...")
response = requests.post(url, json=payload)

print(">>> 响应状态码:", response.status_code)
print(">>> 响应内容:")
print(json.dumps(response.json(), ensure_ascii=False, indent=2))