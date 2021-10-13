# MSDOBot Jenkins

MSDOBot Jenkins 是一個可以讓你從你的 Discord 上獲取 Jenkins Server 上面相關資訊的機器人 Server

## 前置作業

### Discord 相關

#### Discord bot user token

在開始使用這個 bot 之前，我們會需要先幫 bot 取得一個 discord bot user 的身分

到 Discord Developer Portal > Application > New Application

輸入 application name 之後，到 Bot > Add Bot

這樣就成功建立出一隻屬於你的 bot 了，這時你可以在這裡取得你的 bot 的 token

![](https://i.imgur.com/s7M89Dm.png)

如果想要更改機器人的名字的話，可以在 USERNAME 的地方更改，以上圖為例，機器人的名字即為`test bot 1`

#### Discord bot invite link

接下來你需要取得邀請連結，這樣可以把這隻 bot 邀請進你的 Discord Server 中

到左邊的 OAuth2 > OAuth2 Url Generator，在 Scope 選擇 bot 、 Bot Permission 選擇 Administrator，畫面中間就會出現你的邀請連結

![](https://i.imgur.com/SVZ51vZ.png)

現在你就可以過這個連結把 bot 加到你可授權的 Discord Server 中了

#### Discord server ID

你可以在你的 Discord Server 取得你的 Server ID

![](https://i.imgur.com/aid0v52.png)


### 專案設定

你需要將你的 Jenkins Server 的位置等相關資料加到專案的 application.properties 裡面，這樣機器人才知道要去哪裡取得資料

```properties
# discord server information
server.address=localhost
server.port=8080
discord.server.id=<your discord server id>
discord.application.token=<your discord application token>
discord.channel.system=bot-office
discord.channel.rabbitmq=Jenkins-Msg

# rabbitmq configuration
spring.rabbitmq.host=localhost
spring.rabbitmq.port=10011

# rasa configuration
env.setting.rasa.url=http://localhost:5005

# long message configuration
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=msdobot
spring.data.mongodb.username=admin
spring.data.mongodb.password=admin

# customize setting
additional.setting.path=src/main/resources/static/QuizList.yaml
env.setting.jenkins.url=<your jenkins server endpoint>
env.setting.jenkins.user=<your jenkins user name>
env.setting.jenkins.token=<your jenkins token/user password>
```
原則上只需要填入你的 Jenkins Server 的相關資訊，以及剛剛上面提到的 Discord 相關資料就可以了


## 部署
```sh
sh ./build.sh
```

## 建置
```sh
sh ./run.sh
```

## 功能說明

目前的機器人總共有提供下述的功能來幫助使用者來了解連接的`jenkins`的狀況

需要注意的是，這個機器人目前還沒有實作對於預期外對話的處理，所以目前只能處理範圍內對話的回應

如果接收到預期外的對話，機器人也會試圖判斷並回應出被判斷為"最相近"的對話

這個機器人目前只有提供英文的對話功能

以下有提供一些使用的範例，如果輸入的訊息語意沒有差太多應該都可以回應出正確對應的訊息

`help`  取得使用說明

![](https://i.imgur.com/Sd1NvuH.png)

`jenkins view list` 取得 view 列表

![](https://i.imgur.com/Mw8bwy8.png)

`view detail about <ViewName>` 取得 View 細節

![](https://i.imgur.com/WZoTciH.png)

`test report overview of <ViewName>` 取得 View 整體專案的測試報告整合

![](https://i.imgur.com/i0SJmEL.png)

`all jbo list` 取得 job 列表

![](https://i.imgur.com/NV7oiNS.png)

`health report about <JobName>` 取得 Job 建置狀況

![](https://i.imgur.com/cryGfYj.png)

`build result about <JobName>` 取得 Job 建置結果

![](https://i.imgur.com/6pao8c0.png)

`test report about <JobName>` 取得 Job 測試報告

![](https://i.imgur.com/yxkk3Wu.png)

`last build report about <JobName>` 取得 Job 建置報告

![](https://i.imgur.com/NtdUg3J.png)

`jenkins system latest build` 取得最新建置專案

![](https://i.imgur.com/X7lxvnK.png)

`jenkins system failed build` 取得近期失敗專案

![](https://i.imgur.com/N23JXRH.png)

`all jenkins build` 取得近期建置專案

![](https://i.imgur.com/wOSmZSp.png)

`jenkins log` 取得系統紀錄

![](https://i.imgur.com/V1mpsxy.png)

`jenkins plugin` 取得系統插件資訊

![](https://i.imgur.com/kzS8gvH.png)


## 備註

### Rasa 個人化

MSDOBot Jenkins 使用 Rasa Open Source 來對輸入進行分析，如果需要更貼近個人的使用需要更改 Rasa 的設定檔

相關的設定放在`rasa`資料夾中

在更改完設定之後需要執行

```
rasa train --fixed-model-name jenkins
```

這樣才會建立新的 Rasa 模型，模型建立完後重新建置即可

## 使用方式

如果不想進行安裝的話，這裡有提供連結可以直接體驗實際的操作

### 快速體驗

https://discord.gg/ZecVax9sm9

直接加入測試的server就可以跟機器人互動了

需要注意的是測試用的伺服器中的機器人只有連結測試用的jenkins與專案
