import yaml
import os
import threading

class LLMConfig:
    _instance = None
    _lock = threading.Lock()

    def __new__(cls, config_path='llm_conf.yaml'):
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
            raise FileNotFoundError(f"配置文件不存在: {full_path}")

        with open(full_path, 'r', encoding='utf-8') as f:
            config = yaml.safe_load(f)

        llm_section = config.get('llm', {})
        if not llm_section:
            raise ValueError("配置文件中未找到 'llm' 字段")

        self.llm_configs = llm_section

    def get(self, llm_name: str) -> dict:
        """获取指定模型配置"""
        return self.llm_configs.get(llm_name, {})

    def all(self) -> dict:
        """获取所有 LLM 配置"""
        return self.llm_configs

    def list_names(self) -> list:
        """返回所有 LLM 名称"""
        return list(self.llm_configs.keys())

if __name__ == '__main__':
    LLMConfig = LLMConfig()
    print((LLMConfig.list_names()))
    print(LLMConfig.all())
    print(LLMConfig.get('ollama'))
    print(LLMConfig.get('kimi'))
