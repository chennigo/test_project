import yt_dlp
import os
import threading
from concurrent.futures import ThreadPoolExecutor, as_completed
import time

def read_urls_from_file(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        urls = [line.strip() for line in f if line.strip()]
    return urls

def download_video(url, output_dir, thread_id=0):
    ydl_opts = {
        'format': 'bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best',
        'outtmpl': os.path.join(output_dir, '%(title)s.%(ext)s'),
        'merge_output_format': 'mp4',
        'quiet': False,
        'no_warnings': False,
        'ignoreerrors': True,
        'retries': 3,
        'fragment_retries': 3,
        'external_downloader_args': ['-x', '16', '-k', '1M'],
        'http_headers': {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
            'Referer': 'https://www.bilibili.com/',
            'Cookie': 'CURRENT_QUALITY=80;b_lsid=B1E2DA96_19EAA71C7E9;theme-tip-show=SHOWED;bmg_af_sc={"none":{"on":1,"def":"i1.hdslb.com"},"sgp":{"on":1,"def":"i1-sgp.hdslb.com"}};home_feed_column=4;LIVE_BUVID=AUTO5217796385780229;buvid4=674119EB-AAF0-8B3A-A7CE-AB3DDBDC370438000-025112909-iQJrAgReV9FyNvhGbxiV1A%3D%3D;CURRENT_FNVAL=16;buvid3=7E8CB56D-9812-CD44-2E65-9DA93E27F24037222infoc;SESSDATA=94b59317%2C1796518240%2C8576c%2A62CjAdgTjMnld14sZrXpyGE7ynunA2Hl4htks7g5MQSYa-zB0OVp6V1qAuhIciV5Tr0cASVlVidUVja0Q2Y0VGWERsSFRPWEI1VWc2YU5xbHdvQW5CRGZIaGZPLV9RWWd3Q091a1dkY1J6cXlZTnVER0hTb3E1aFNvaWo4UnlwZDBWUDNldlM5NG1nIIEC;theme-avatar-tip-show=SHOWED;b_nut=1764380137;_uuid=C82FD63D-C9310-34EA-6596-97D62E6D596B36941infoc;bili_jct=a035966ae339f4c4d712eba61d70d121;bili_ticket=eyJhbGciOiJIUzI1NiIsImtpZCI6InMwMyIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3ODEyMjU0MzEsImlhdCI6MTc4MDk2NjE3MSwicGx0IjotMX0.GWMI7pfnoqgtviz_zQW9-2sQt1Zs_sR7HHZ_gxwgViM;bili_ticket_expires=1781225371;bmg_af_switch=1;bmg_src_def_domain=i1.hdslb.com;bp_t_offset_180167295=1211744861296787456;browser_resolution=1302-760;buvid_fp=f9526b1340a9a2118f75052bac702a8f;DedeUserID=180167295;DedeUserID__ckMd5=7cc994682f23741f;hit-dyn-v2=1;PVID=1;rpdid=|(um~lJRJuYJ0J\'u~Yl|~YR~R',
        },
        'nocheckcertificate': True,
    }
    
    try:
        with yt_dlp.YoutubeDL(ydl_opts) as ydl:
            print(f"线程 {thread_id} 开始下载: {url}")
            start_time = time.time()
            ydl.download([url])
            elapsed = time.time() - start_time
            print(f"线程 {thread_id} 下载完成: {url} (耗时: {elapsed:.2f}秒)")
            return True
    except Exception as e:
        print(f"线程 {thread_id} 下载失败: {url}")
        print(f"错误信息: {str(e)}")
        return False

def main():
    input_file = 'bilibili_movies_urls.txt'
    output_dir = r'H:\movie'
    max_workers = 4
    
    if not os.path.exists(input_file):
        print(f"错误: 输入文件 {input_file} 不存在")
        return
    
    if not os.path.exists(output_dir):
        os.makedirs(output_dir, exist_ok=True)
        print(f"创建输出目录: {output_dir}")
    
    urls = read_urls_from_file(input_file)
    print(f"共读取到 {len(urls)} 个视频链接")
    
    success_count = 0
    fail_count = 0
    
    with ThreadPoolExecutor(max_workers=max_workers) as executor:
        futures = {executor.submit(download_video, url, output_dir, i+1): (url, i+1) for i, url in enumerate(urls)}
        
        for future in as_completed(futures):
            url, thread_id = futures[future]
            try:
                result = future.result()
                if result:
                    success_count += 1
                else:
                    fail_count += 1
            except Exception as e:
                print(f"线程 {thread_id} 发生异常: {url}")
                print(f"异常信息: {str(e)}")
                fail_count += 1
    
    print("\n=== 下载完成 ===")
    print(f"成功: {success_count} 个")
    print(f"失败: {fail_count} 个")

if __name__ == '__main__':
    main()