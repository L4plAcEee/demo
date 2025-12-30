from openai import OpenAI
from dotenv import load_dotenv
import os
import json

with open('character.json', 'r', encoding='utf-8') as file:
    character = json.load(file)

load_dotenv()

client = OpenAI(
    api_key = os.getenv('MOOSHOT_API_KEY'),
    base_url = os.getenv('MOOSHOT_BASE_URL'),
)
 
system_messages = [
	{"role": "system", "content": "你是人工智能助手，你更擅长中文和英文的对话。你会为用户提供安全，有帮助，准确的回答。"},
]
 
messages = []

 
def make_messages(input: str, n: int = 20, system_role: str = "default") -> list[dict]:
    global messages

    messages.append({
		"role": "user",
		"content": choose_character(system_role) + input,	
	})
 
    new_messages = []
    
    new_messages.extend(system_messages)
 
    if len(messages) > n:
        messages = messages[-n:]

    new_messages.extend(messages)
    return new_messages
 
 
def chat(input: str, system_role: str) -> str:
    completion = client.chat.completions.create(
        model="moonshot-v1-auto",
        messages=make_messages(input, system_role=system_role),
        temperature=0.3,
    )
 
    assistant_message = completion.choices[0].message
 
    messages.append(assistant_message)
 
    return assistant_message.content

def choose_character(role: str) -> str:
    global character
    
    role_data:str = " "
    if(role is not None):
        role_data = json.dumps(character.get('角色', {}).get(role, {}), ensure_ascii=False)
    return role_data


    