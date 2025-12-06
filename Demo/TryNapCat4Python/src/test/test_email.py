import unittest

from src.l4p import list_unseen_raw, parse_message_data
from src.l4p.config.EmailConfig import EmailConfig


class MyTestCase(unittest.TestCase):
    def test_0(self):
        email_conf1 = EmailConfig()
        self.assertIsNotNone(email_conf1)
        email_conf2 = EmailConfig()
        self.assertIsNotNone(email_conf2)
        self.assertEqual(email_conf1, email_conf2)
        email_conf = EmailConfig()
        # 遍历所有邮箱
        for name, cfg in email_conf.all().items():
            print(f"{name} 配置:", cfg)
            raw = list_unseen_raw(cfg)
            self.assertIsNotNone(raw)
            parsed = parse_message_data(raw)
            self.assertIsNotNone(parsed)
            for mail in parsed:
                self.assertIsNotNone(mail)
                print(mail)

if __name__ == '__main__':
    unittest.main()
