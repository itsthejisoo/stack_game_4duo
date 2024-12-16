# stack_game_4duo

## 개요

2인용 탑 쌓기 게임. <br>
2024-2 SSU AI융합학부 모바일 프로그래밍 수업 (김강희 교수님) - 팀프로젝트

## 프로젝트 구성원

<table>
  <tr>
    <th>이름</th>
    <th>프로필 사진</th>
    <th>프로필 링크</th>
  </tr>
  <tr>
    <td>김지수</td>
    <td><img src="https://avatars.githubusercontent.com/u/80537289?v=4" width="50" style="border-radius:100%;"></td>
    <td><a href="https://github.com/itsthejisoo">itsthejisoo</a></td>
  </tr>
  <tr>
    <td>최서정</td>
    <td><img src="https://avatars.githubusercontent.com/u/113536288?v=4" width="50" style="border-radius:100%;"></td>
    <td><a href="https://github.com/bum22">bum22</a></td>
  </tr>
  <tr>
    <td>홍형준</td>
    <td><img src="https://avatars.githubusercontent.com/u/72370753?v=4" width="50" style="border-radius:100%;"></td>
    <td><a href="https://github.com/Hong-lol">Hong-lol</a></td>
  </tr>
</table>
  
## 계획
- **10/11~ 10/28**
  - 프로젝트 주제 선정
  - 역할 분담
  - 제안서 작성
  - 개발 환경 설정
- **10/29 ~ 11/1**
  - ui 디자인, 폰트 xml 추가
  - 게임 로직 구현
- **11/2 ~ 11/13**
  - 상세 기능 구현
  - 시작화면, 종료화면 수정
  - 모드 선택 기능 추가
- **11/14 ~ 11/27**
  - soketio를 사용하여 서버 구축
  - 단말기 간의 게임 서버 연결
- **11/29 ~ 12/9**
  - 각 플레이어의 닉네임을 가져와서 이름과 점수 저장
  - 최종 보고서 NickNameActivity를 제외하고 작성 완료
  - 최종 발표 ppt 제작 완료
- **12/10**
  - 최종 보고서 작성 완료
  - ppt 최종 수정 완료
  - 대본 작성
- **12/15**
  - 교수님께 받은 피드백 일부 수정 (bulky code)
  - 쓰레드가 왜 2개 생성됐는지 물어보셨지만, 상대방 연결 확인과 서로 블록 정보를 동시에 처리하기 위해서 쓰레드가 두개 필요하겠다고 판단하여 수정하지 않음

## 서버 모드 사용 방법

1. GameServer.java를 실행한다.
2. 두 개의 에뮬레이터에서 stack 게임을 실행한다.
3. 모드 선택을 server mode로 하고 본인 넥네임을 Player1에, 상대방 닉네임을 Player2에 작성한다.
4. 상단 화면은 상대방 게임 화면이고, 하단 화면은 본인 게임 화면이다. 하단 화면으로 게임을 즐긴다.

> ### 프로젝트 clone할때 주의사항
>
> - 프로젝트 clone 후 실행시키기 위해서 Code 파일을 열어주십시오.
> - 이 프로젝트는 맥북으로 작업되었기 때문에 윈도우에서 실행시 에러가 발생할 수 있습니다.
>   - `SDK location not found. Define location with an ANDROID_SDK_ROOT environment variable or by setting the sdk.dir path in your project's local properties file` 에러가 발생할 경우 <br> Code 디렉토리에 local.properties 파일을 생성하고 아래와 같이 작성해주십시오. <br> 윈도의 경우: `sdk.dir=c:\Users\'사용자 이름'\AppData\Local\android\adk`<br> 맥의 경우: `sdk.dir=/Users/'사용자 이름'/Library/Android/sdk`
