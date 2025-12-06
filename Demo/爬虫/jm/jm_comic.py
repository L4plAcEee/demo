import jmcomic

jm_album_id: str = '422866'

# 创建配置对象
option = jmcomic.create_option_by_file('./conf.yml')

client = option.build_jm_client()

album = client.get_album_detail(jm_album_id)

option.download_album(jm_album_id)
# 使用option对象来下载本子
# 等价写法: option.download_album(422866)
# jmcomic.download_album(422866, option)
