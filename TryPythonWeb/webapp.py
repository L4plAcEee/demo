from fastapi import FastAPI, HTTPException
from sqlalchemy import Column, Integer, String, create_engine
from sqlalchemy.orm import declarative_base, sessionmaker
import uvicorn
import sqlite3
import threading
from pydantic import BaseModel

app = FastAPI()


class User(BaseModel):
    __tablename__ = 'users'
    __table_args__ = {'extend_existing': True}  # 允许覆盖现有
    # 定义表结构
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, index=True)
    age = Column(Integer)
    email = Column(String, unique=True, index=True)
    password = Column(String)

@app.get("/", status_code=200)
def root():
    return {"message": "Hello, World!"}

@app.get("/user/{email}", status_code=200, response_model=User)
def get_user_by_email(email: str):
    check_table_exists('users')  # 确保表存在
    if not check_table_exists('users'):
        create_user_table()
    conn = DBConnection.instance().get_connection()
    cursor = conn.cursor()
    cursor.execute("SELECT * FROM users WHERE email = ?", (email,))
    user_row = cursor.fetchone()
    cursor.close()
    if not user_row:
        raise HTTPException(
            status_code=404,
            detail="user not found"
        )
    return User(name=user_row['name'], age=user_row['age'], email=user_row['email'], password=user_row['password'])




@app.post("/user/", status_code=200)
def create_user(user: User):
    check_table_exists('users')  # 确保表存在
    if not check_table_exists('users'):
        create_user_table()
    conn = DBConnection.instance().get_connection()
    cursor = conn.cursor()
    cursor.execute("INSERT INTO users (name, age, email, password) VALUES (?, ?, ?, ?)",
                   (user.name, user.age, user.email, user.password))
    conn.commit()
    cursor.close()
    return {"message": "User created successfully"}
if __name__ == "__main__":
    Base = declarative_base()
    engine = create_engine("sqlite:///example.db")
    Session = sessionmaker(bind=engine)
    session = Session()

    Base.metadata.create_all(engine)  # 创建表
    uvicorn.run("webapp:app", host="localhost", port=8000, reload=True, workers=1)