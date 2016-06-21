# Facebook Tracking #

利用 Facebook Graph API 來模擬一位已授權的用戶，
來爬取公開的資訊。


由於 FB 修改隱私權後，已無法直接用 Graph API 取的未授權的用戶基本資料，
改由用 Selenium 透過瀏覽器取得。


執行指定： `run -Dfacebook.appid=FB_APPID -Dfacebook.secret=FB_APP_SECRET -Dfacebook.account=FB_ACCOUNT -Dfacebook.password=FB_PASSWORD -Dinstagram.appid=INSTAGRAM_CLIENT_ID -Dinstagram.secret=INSTAGRAM_CLIENT_SECRET`


Instagram 也因此修改設定，目前無法在開發模式，直接取得用戶資料。

