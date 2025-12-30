"""
这种写法通常被称为 方法链 (Method Chaining) 或 链式调用。


它是一种编程风格，其中每个方法都返回一个对象（通常是this，即调用该方法的对象本身），
从而允许在一次操作中连续调用多个方法，而无需将每个中间结果存储在变量中。


这种风格也与 流式接口 (Fluent Interface)
的设计思想密切相关，其目标是让代码读起来更像自然语言，更加流畅和易于理解。


优点:
* 简洁性: 代码更短，减少了重复的变量名。
* 可读性: 代码结构清晰，可以像一句话一样从左到右阅读。
"""

class User:
    def __init__(self):
        self.name: str = ""
        self.age: int = 0

    def setName(self, name: str ) -> "User":
        self.name = name
        return self
    
    def setAge(self, age: int) -> "User":
        self.age = age
        return self
    
    def sayHi(self) -> "User":
        print(f"Hi! I am {self.name}, i am {self.age} olds")
        return self

if __name__ == "__main__":
    User().setName("王尼玛").setAge(12).sayHi()
    User().setName("王尼美").setAge(11).sayHi()