import re
from ..config.EmailConfig import EmailConfig
from ..utils.email import list_unseen_raw, parse_message_data
from ..utils.llm import conversation_with_llm

MODEL_NAME = 'kimi'

def private_chat(msg: str) -> str:
    if bool(re.fullmatch(r'\s*/总结邮箱\s*', msg)):
        return _all_email_summarized_by_llm(model_name=MODEL_NAME)
    return conversation_with_llm(model_name=MODEL_NAME, prompt=msg)

def _all_email_summarized_by_llm(model_name: str="ollama") -> str:
    email_conf = EmailConfig()
    email_names = email_conf.list_names()
    if not email_names:
        raise ValueError("未配置任何邮箱")

    all_summaries = []

    for name in email_names:
        prompt:str = ""
        prompt += f"回复格式：请不要使用 Markdown 格式，而是普通文本格式；\n"
        prompt += "根据以下三点进行分析：1. 统计邮件分布 2. 分析邮件主题 3. 总结邮件内容\n"
        prompt += f"请总结以下 {name} 邮箱的内容摘要\n"

        parsed = parse_message_data(list_unseen_raw(conf=email_conf.get(name)))
        for mail in parsed:
            prompt += f"{mail}\n"

        summary = conversation_with_llm(model_name=model_name, prompt=prompt)

        all_summaries.append(f"===== {name} 邮箱摘要 =====\n{summary}\n")

    summarized = "\n\n".join(all_summaries)
    return summarized

if __name__ == '__main__':
    common_msg = r"你是谁？"
    command_msg = r"/总结邮箱"
    print(private_chat(common_msg))
    print(private_chat(command_msg))