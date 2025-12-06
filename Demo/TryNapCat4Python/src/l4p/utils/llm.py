from ..config.LLMConfig import LLMConfig
from openai import OpenAI

def conversation_with_llm(model_name: str="ollama", prompt: str="") -> str:
    llm_conf = LLMConfig().get(model_name)
    if not llm_conf:
        raise ValueError(f"未找到名为 {model_name} 的 LLM 配置")

    llm_client = OpenAI(
        base_url=llm_conf["base_url"],
        api_key=llm_conf["api_key"],
    )
    try:
        response = llm_client.chat.completions.create(
            model=llm_conf["model_name"],
            messages=[{"role": "user", "content": prompt}],
            temperature=llm_conf.get("temperature", 0.7)
        )
        ret = response.choices[0].message.content.strip()
    except Exception as e:
        ret = f"生成失败: {e}"
    return ret

