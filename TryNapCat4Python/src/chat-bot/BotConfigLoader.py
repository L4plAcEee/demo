import yaml
import os

class BotConfigLoader:
    def __init__(self):
        config_path = os.path.join(os.path.dirname(__file__), 'chat_bot_conf.yaml')
        with open(config_path, 'r', encoding='utf-8') as file:
            conf = yaml.safe_load(file)
            self.allow_group: list = conf.get('allow_group', [])
            self.allow_private: list = conf.get('allow_private', [])