import yaml
import os
import threading


class EmailConfig:
    _instance = None
    _lock = threading.Lock()

    def __new__(cls, config_path='email_conf.yaml'):
        if not cls._instance:
            with cls._lock:
                if not cls._instance:
                    cls._instance = super().__new__(cls)
                    cls._instance._init_config(config_path)
        return cls._instance

    def _init_config(self, config_path):
        base_dir = os.path.dirname(os.path.dirname(os.path.dirname(__file__)))
        full_path = os.path.join(base_dir, 'resource', config_path)

        if not os.path.exists(full_path):
            raise FileNotFoundError(f"邮箱配置文件不存在: {full_path}")

        with open(full_path, 'r', encoding='utf-8') as f:
            config = yaml.safe_load(f)

        self._email_dict = {}

        for email_cfg in config.get('emails', []):
            name = email_cfg.get('name')
            if not name:
                continue
            self._email_dict[name] = email_cfg  # 保留原始字段

    def get(self, name: str) -> dict:
        """获取指定邮箱配置"""
        return self._email_dict.get(name, {})

    def all(self) -> dict:
        """获取所有邮箱配置（name → dict）"""
        return self._email_dict.copy()

    def list_names(self) -> list:
        """列出所有邮箱名称"""
        return list(self._email_dict.keys())

if __name__ == '__main__':
    email_conf = EmailConfig()

    # 获取指定邮箱配置
    qq1 = email_conf.get("1775906806@qq.com")
    print(qq1["imap_server"], qq1["account"])

    # 遍历所有邮箱
    for name, cfg in email_conf.all().items():
        print(f"{name} 配置:", cfg)