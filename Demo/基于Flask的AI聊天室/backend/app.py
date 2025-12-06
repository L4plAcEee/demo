from flask import Flask, request, jsonify
from flask_cors import CORS
import chat
from dotenv import load_dotenv
import os

load_dotenv()

app = Flask(__name__)
CORS(app)

 
@app.route('/api/chat', methods=['POST'])
def main():
    data = request.json
    message = data.get('message', '')
    role = data.get('role', 'default')
    
    response = f"[{role}]:{chat.chat(message, system_role=role)}"
    
    return jsonify({"response": response})

if __name__ == '__main__':
    app.run(host=os.getenv('PRODUCT_HOST'), port=5000)
    # app.run(host=os.getenv('DEVELOP_HOST'), port=5000)