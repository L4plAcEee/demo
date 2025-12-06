from ncatbot.core import BotClient
from ncatbot.core.message import PrivateMessage
from ncatbot.utils.logger import get_log
from BotConfigLoader import BotConfigLoader
from src.l4p.api.chat_bot import private_chat

_log = get_log()
bot_config: BotConfigLoader = BotConfigLoader()


bot = BotClient()


@bot.private_event()
async def on_private_message(msg: PrivateMessage):
    if msg.user_id not in bot_config.allow_private or msg.raw_message is None:
        return
    _log.info(msg)
    await bot.api.post_private_msg(user_id=msg.user_id, text=private_chat(msg.raw_message))


if __name__ == "__main__":
    bot.run()