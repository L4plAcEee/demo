from ..model.EmailDTO import EmailDTO

from imapclient import IMAPClient
import email
from email.message import Message
from typing import List

def parse_message_data(messages: List[Message]) -> List[EmailDTO]:
    """
    接收 Message 格式的邮件列表，解析为 EmailDTO
    """
    parsed_list = []

    for msg in messages:
        text = ''
        if msg.is_multipart():
            for part in msg.walk():
                content_type = part.get_content_type()
                content_disposition = part.get("Content-Disposition", "")
                if content_type == "text/plain" and "attachment" not in content_disposition:
                    charset = part.get_content_charset() or 'utf-8'
                    try:
                        text = part.get_payload(decode=True).decode(charset, errors='ignore')
                        break
                    except Exception as e:
                        print(f"[正文解析失败] {e}")
        else:
            charset = msg.get_content_charset() or 'utf-8'
            try:
                text = msg.get_payload(decode=True).decode(charset, errors='ignore')
            except Exception as e:
                print(f"[解码失败] {e}")

        cleaned_text = '\n'.join(line.strip() for line in text.splitlines() if line.strip())

        parsed_list.append(
            EmailDTO(
                subject=msg.get('Subject'),
                sender=msg.get('From'),
                to_email=msg.get('To'),
                date=msg.get('Date'),
                text=cleaned_text
            )
        )

    return parsed_list


def list_unseen_raw(conf: dict) -> list[Message]:
    """
    接受配置信息，返回 RFC822格式 解析后的的 Message 列表
    :param conf: dict
    :return: list[Message]
    """
    all_emails: list[Message] = []
    host = conf['imap_server']
    account = conf['account']
    password = conf['password']

    with IMAPClient(host, ssl=True) as client:
        client.login(account, password)
        client.select_folder('INBOX')

        messages = client.search(['UNSEEN'])
        print(f"邮箱 {account} 找到 {len(messages)} 份未读邮件")

        for uid, message_data in client.fetch(messages, ['RFC822']).items():
            msg = email.message_from_bytes(message_data[b'RFC822'])
            all_emails.append(msg)

    return all_emails

if __name__ == '__main__':
    conf =  {'imap_server' : 'imap.qq.com', 'account' : '1906518568@qq.com', 'password' : 'szjdznyofysgejjd'}
    raw_list = list_unseen_raw(conf)
    parsed = parse_message_data(raw_list)

    for mail in parsed:
        print(mail)
