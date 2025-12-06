"""
#TODO
1. 或许可能要改成单例模式？
2. 保存地址还没改 默认是脚本当前目录下./output/中
3. 测试用例

项目地址 https://github.com/hect0x7/JMComic-Crawler-Python
"""
import jmcomic

CONFIG_PATH: str = r'../resource/jm_conf.yml'

def get_single_album(jm_album_id: str):
    # 创建配置对象
    option = jmcomic.create_option_by_file(CONFIG_PATH)

    option.download_album(jm_album_id)
    # 使用option对象来下载本子
    # 等价写法: option.download_album(422866)
    # jmcomic.download_album(422866, option)


if __name__ == '__main__':
    jm_album_id: str = '422866'
    get_single_album(jm_album_id)