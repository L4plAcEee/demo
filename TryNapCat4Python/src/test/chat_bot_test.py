import unittest

from src.l4p.api.chat_bot import private_chat


class MyTestCase(unittest.TestCase):
    def test_private_chat(self):
        msg: str = '你是谁？'
        res = private_chat(msg=msg)
        self.assertIsNotNone(res)




if __name__ == '__main__':
    unittest.main()
