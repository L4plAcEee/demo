import requests

response = requests.get("http://localhost:8000/")
print(response.status_code)
print(response.json())  # 以 dict 形式解析 JSON 响应

response = requests.get("http://localhost:8000/user/test@example.com")
print(response.status_code)
print(response.json())  # 以 dict 形式解析 JSON 响应


response = requests.post("http://localhost:8000/user/", json={
    "name": "Test User",
    "age": 30,
    "email": "test@example.com",
    "password": "securepassword"
})

print(response.status_code)
print(response.json())  # 以 dict 形式解析 JSON 响应

response = requests.get("http://localhost:8000/user/test@example.com")
print(response.status_code)
print(response.json())  # 以 dict 形式解析 JSON 响应


