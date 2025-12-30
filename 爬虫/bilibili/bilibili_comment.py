"""
核心逻辑来自：
CSDN项目地址：(https://blog.csdn.net/onedyer/article/details/146535940?spm=1011.2124.3001.6209)
博客教程地址：(https://blog.ldyer.top/2025/03/26/B%E7%AB%99%E8%AF%84%E8%AE%BA%E7%88%AC%E5%8F%96/)
"""
import asyncio
import os
import re
import requests
import json
from urllib.parse import quote
import pandas as pd
import hashlib
import urllib
import time
import csv

# === === 全局参数 === ===
COOKIE: str = r"buvid3=A81B2C7C-DAF5-3B84-2A5C-7ECC0C87878757322infoc; b_nut=1731832857; _uuid=102912124-101019-88E4-D10C5-9FE3CDDD5E5963304infoc; buvid4=04251A20-56F5-1B8D-C60F-A8840A0E5AF863645-024111708-0lKJqwtJRGBuwNvSg1OGSA%3D%3D; header_theme_version=CLOSE; enable_web_push=DISABLE; rpdid=0zbfvUtgCk|2jysIvil|1g8|3w1TdMar; buvid_fp_plain=undefined; CURRENT_QUALITY=112; LIVE_BUVID=AUTO3217336547023204; hit-dyn-v2=1; enable_feed_channel=ENABLE; fingerprint=74332ba89fbe82edbb04a6a83cf5305d; buvid_fp=8f2d90fe0b7520f7ccd0144681e595d2; DedeUserID=356541238; DedeUserID__ckMd5=297f4240efeff698; PVID=1; bili_ticket=eyJhbGciOiJIUzI1NiIsImtpZCI6InMwMyIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3NTAxNjA4NjIsImlhdCI6MTc0OTkwMTYwMiwicGx0IjotMX0.FokZYWRyZwX4B8-xi4M1sLSXM9nOUXAgYh2hculneRg; bili_ticket_expires=1750160802; SESSDATA=8936146c%2C1765453668%2C3a663%2A61CjAw-e3G2KT8e0eA1kBox29ERQyMS_w0xhH2eHF7Hu2RFxaWszE_xyvspIdRUOGCZhMSVmRHWi1nWlpURERLcG5Xdmg4RGszT1Bma3BxVldtZHhIZDZYY3JURDVzdjZ4b2pheE5QdE9UZ250QkwyaVRBUm9wZlVwYWs1RXoycmpVSWNmLVBja013IIEC; bili_jct=b7ec2d6514cf3ce67adfc314690f2090; sid=89fb2vql; CURRENT_FNVAL=4048; bp_t_offset_356541238=1078351131362459648; b_lsid=D841CC54_197724EA406; home_feed_column=4; browser_resolution=1144-746"
OUTPUT_PATH: str = r".\output" # 相对或者绝对
WAIT_TIME: float = 0.5 # 防反爬 每次停顿 单位：秒
# === === 全局参数 === ===

# 获取B站的Header
def get_header():
    # with open('bili_cookie.txt','r') as f:
    #         cookie=f.read()
    header={
            "Cookie":COOKIE,
            "User-Agent":'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36 Edg/134.0.0.0'
    }
    return header

# 通过bv号，获取视频的oid
def get_information(bv):
    resp = requests.get(f"https://www.bilibili.com/video/{bv}/?p=14&spm_id_from=pageDriver&vd_source=cd6ee6b033cd2da64359bad72619ca8a", headers=get_header())
    # 提取视频oid
    obj = re.compile(f'"aid":(?P<id>.*?),"bvid":"{bv}"')
    oid = obj.search(resp.text).group('id')

    # 提取视频的标题
    obj = re.compile(r'<title data-vue-meta="true">(?P<title>.*?)</title>')
    try:
        title = obj.search(resp.text).group('title')
    except:
        title = "未识别"

    return oid,title

# MD5加密
def md5(code):
    MD5 = hashlib.md5()
    MD5.update(code.encode('utf-8'))
    w_rid = MD5.hexdigest()
    return w_rid

# 轮页爬取
async def start(bv, oid, pageID, count, csv_writer, is_second):
    # 参数
    mode = 2   # 为2时爬取的是最新评论，为3时爬取的是热门评论
    plat = 1
    type = 1  
    web_location = 1315875

    # 获取当下时间戳
    wts = int(time.time())
    
    # 如果不是第一页
    if pageID != '':
        pagination_str = '{"offset":"%s"}' % pageID
        code = f"mode={mode}&oid={oid}&pagination_str={urllib.parse.quote(pagination_str)}&plat={plat}&type={type}&web_location={web_location}&wts={wts}" + 'ea1db124af3c7062474693fa704f4ff8'
        w_rid = md5(code)
        url = f"https://api.bilibili.com/x/v2/reply/wbi/main?oid={oid}&type={type}&mode={mode}&pagination_str={urllib.parse.quote(pagination_str, safe=':')}&plat=1&web_location=1315875&w_rid={w_rid}&wts={wts}"
    
    # 如果是第一页
    else:
        pagination_str = '{"offset":""}'
        code = f"mode={mode}&oid={oid}&pagination_str={urllib.parse.quote(pagination_str)}&plat={plat}&seek_rpid=&type={type}&web_location={web_location}&wts={wts}" + 'ea1db124af3c7062474693fa704f4ff8'
        w_rid = md5(code)
        url = f"https://api.bilibili.com/x/v2/reply/wbi/main?oid={oid}&type={type}&mode={mode}&pagination_str={urllib.parse.quote(pagination_str, safe=':')}&plat=1&seek_rpid=&web_location=1315875&w_rid={w_rid}&wts={wts}"
    

    comment = requests.get(url=url, headers=get_header()).content.decode('utf-8')
    comment = json.loads(comment)

    for reply in comment['data']['replies']:
        # 评论数量+1
        count += 1

        if count % 1000 ==0:
            time.sleep(20)

        # 上级评论ID
        parent=reply["parent"]
        # 评论ID
        rpid = reply["rpid"]
        # 用户ID
        uid = reply["mid"]
        # 用户名
        name = reply["member"]["uname"]
        # 用户等级
        level = reply["member"]["level_info"]["current_level"]
        # 性别
        sex = reply["member"]["sex"]
        # 头像
        avatar = reply["member"]["avatar"]
        # 是否是大会员
        if reply["member"]["vip"]["vipStatus"] == 0:
            vip = "否"
        else:
            vip = "是"
        # IP属地
        try:
            IP = reply["reply_control"]['location'][5:]
        except:
            IP = "未知"
        # 内容
        context = reply["content"]["message"]
        # 评论时间
        reply_time = pd.to_datetime(reply["ctime"], unit='s')
        # 相关回复数
        try:
            rereply = reply["reply_control"]["sub_reply_entry_text"]
            rereply = int(re.findall(r'\d+', rereply)[0])
        except:
            rereply = 0
        # 点赞数
        like = reply['like']

        # 个性签名
        try:
            sign = reply['member']['sign']
        except:
            sign = ''

        # 写入CSV文件
        csv_writer.writerow([count, parent, rpid, uid, name, level, sex, context, reply_time, rereply, like, sign, IP, vip, avatar])

        # 二级评论(如果开启了二级评论爬取，且该评论回复数不为0，则爬取该评论的二级评论)
        if is_second and rereply !=0:
            for page in range(1,rereply//10+2):
                second_url=f"https://api.bilibili.com/x/v2/reply/reply?oid={oid}&type=1&root={rpid}&ps=10&pn={page}&web_location=333.788"
                second_comment=requests.get(url=second_url, headers=get_header()).content.decode('utf-8')
                second_comment=json.loads(second_comment)
                for second in second_comment['data']['replies']:
                    # 评论数量+1
                    count += 1
                    # 上级评论ID
                    parent=second["parent"]
                    # 评论ID
                    second_rpid = second["rpid"]
                    # 用户ID
                    uid = second["mid"]
                    # 用户名
                    name = second["member"]["uname"]
                    # 用户等级
                    level = second["member"]["level_info"]["current_level"]
                    # 性别
                    sex = second["member"]["sex"]
                    # 头像
                    avatar = second["member"]["avatar"]
                    # 是否是大会员
                    if second["member"]["vip"]["vipStatus"] == 0:
                        vip = "否"
                    else:
                        vip = "是"
                    # IP属地
                    try:
                        IP = second["reply_control"]['location'][5:]
                    except:
                        IP = "未知"
                    # 内容
                    context = second["content"]["message"]
                    # 评论时间
                    reply_time = pd.to_datetime(second["ctime"], unit='s')
                    # 相关回复数
                    try:
                        rereply = second["reply_control"]["sub_reply_entry_text"]
                        rereply = re.findall(r'\d+', rereply)[0]
                    except:
                        rereply = 0
                    # 点赞数
                    like = second['like']
                    # 个性签名
                    try:
                        sign = second['member']['sign']
                    except:
                        sign = ''

                    # 写入CSV文件
                    csv_writer.writerow([count, parent, second_rpid, uid, name, level, sex, context, reply_time, rereply, like, sign, IP, vip, avatar])
    # 下一页的pageID
    try:
        next_pageID = comment['data']['cursor']['pagination_reply']['next_offset']
    except:
        next_pageID = 0

    # 判断是否是最后一页了
    if next_pageID == 0:
        print(f"评论爬取完成！总共爬取{count}条。")
        return bv, oid, next_pageID, count, csv_writer,is_second
    # 如果不是最后一页，则停0.5s（避免反爬机制）
    else:
        time.sleep(WAIT_TIME)
        print(f"当前爬取{count}条。")
        return bv, oid, next_pageID, count, csv_writer,is_second

async def main(bv: str):
    print(f"当前爬取{bv}")
    # 获取视频oid和标题
    oid,title = get_information(bv)
    # 评论起始页（默认为空）
    next_page_id = ''
    # 初始化评论数量
    count = 0
    # 是否开启二级评论爬取，默认开启
    is_second = True

    file_path = os.path.join(OUTPUT_PATH, re.sub(r'[\\/:*?"<>|]', '-', f"{bv}_{title}_comments.csv"))

    # 创建CSV文件并写入表头
    with open(file_path, mode='w', newline='', encoding='utf-8-sig') as file:
        csv_writer = csv.writer(file)
        csv_writer.writerow(['序号', '上级评论ID','评论ID', '用户ID', '用户名', '用户等级', '性别', '评论内容', '评论时间', '回复数', '点赞数', '个性签名', 'IP属地', '是否是大会员', '头像'])

        # 开始爬取
        while next_page_id != 0:
            bv, oid, next_page_id, count, csv_writer,is_second = await start(bv, oid, next_page_id, count, csv_writer,is_second)

async def run_all(bv_list: list):
    task: list = []
    for bv in bv_list:
        task.append(main(bv))
    await asyncio.gather(*task)

if __name__ == "__main__":
    os.makedirs(OUTPUT_PATH, exist_ok=True)
    target = [
        "BV1f7JjzCEbB"
    ]
    asyncio.run(run_all(target))

