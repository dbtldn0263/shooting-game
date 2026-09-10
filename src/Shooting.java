import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
class Frame extends JFrame implements KeyListener, Runnable{

	int f_width ;//프레임의 넓이
	int f_height ;//프레임의 높이
	int x, y; //캐릭터 좌표변수
	boolean KeyUp = false;//키보드 입력 처리를 위한 변수
	boolean KeyDown = false;
	boolean KeyLeft = false;
	boolean KeyRight = false;
	boolean KeySpace = false;//마법 발사를 스페이스에서 함
	int cnt;// 타이밍 조절을 위한 무한 루프 카운터 변수
	int game_Score;//게임점수
	int player_HP;//체력
	int Magic_Speed;//마법 빠르기
	Thread th;//스레드 생성

	Image Player_img;//이미지 변수들
	Image Magic_img;
	Image Enemy_img;  
	Image Background_img;
	ArrayList Magic_List = new ArrayList();
	ArrayList Enemy_List = new ArrayList();
	ArrayList Hit_List = new ArrayList();
	Image buffImage; Graphics buffg;//더블 버퍼링용
	Hit hit; //충돌 클래스 접근 키
	Magic Magic;//마법 클래스 접근 키
	Enemy en;//디멘터 접근키
	JPanel endpanel=new JPanel();
	JPanel scorepanel=new JPanel();
	JFrame f=new JFrame();
	JFrame e=new JFrame();

	Frame(){
		init();
		go();
		setTitle("해리포터");
		setSize(f_width, f_height);
		setResizable(false);//창 크기 변경 못하게함
		setVisible(true);

	}

	public void init(){ 
		x = 100;//캐릭터 최초 좌표
		y = 100;
		f_width = 1200;
		f_height = 750;

		Magic_img = new ImageIcon("패트로누스편집.png").getImage();
		Enemy_img = new ImageIcon("디멘터편집.png").getImage();
		Player_img = new ImageIcon("harrypotter편집.png").getImage();
		Background_img = new ImageIcon("호그와트 밤 배경.png").getImage();
		game_Score=0;//점수 초기값
		player_HP=3;//체력 초기값
		Magic_Speed=4;//마법 빠르기 숫자 작아지면 연사 속도가 빨라짐
	}
	public void go(){
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addKeyListener(this);//키보드 이번트
		th = new Thread(this); //스레드 생성
		th.start(); //스레드 실행

	}

	public void run(){ //스레드 무한 루프될 곳
		try{ //
			while(true){//무한 루프
				KeyProcess(); //키보드 입력처리를 하여 x,y 갱신
				EnemyProcess();//디멘터 움직임 처리 메소드 실행
				MagicProcess(); //마법 처리 메소드 실행

				repaint(); //갱신된 x,y값으로 이미지 새로 그림

				Thread.sleep(20);//20 밀리초로 스레드 돌림
				cnt ++;
			}
		}catch (Exception e){}
	}

	public void MagicProcess(){ //마법 처리 메소드
		if ( KeySpace ){//스페이스 키가 눌리면 
			if((cnt%Magic_Speed)==0) {//연사 속도 조절
				Magic = new Magic(x+70, y+10);
				//마법 발사 위치를 제대로 입으로 하기 위한 좌표조정
				Magic_List.add(Magic); //미사일 추가
			}
		}
		for ( int i = 0 ; i < Magic_List.size() ; ++i){
			Magic = (Magic) Magic_List.get(i);
			Magic.move();
			if ( Magic.x > f_width - 20 ){
				Magic_List.remove(i);
			}
			//편의상 그림그리기 부분에 있던 마법의 이동과 마법이 화면에서 벗어났을시 명령 처리를
			for (int j = 0 ; j < Enemy_List.size(); ++ j){
				en = (Enemy) Enemy_List.get(j);
				if (Crash(Magic.x,Magic.y,en.x,en.y,Magic_img, Enemy_img)){
					//마법과 적 객체를 하나하나 판별하여
					//접촉했을시 마법과 적을 화면에서 지움
					//충돌판정 메소드로 넘기고 t or f값을 받아서 t면 아래 실행
					//판별엔 Crash 메소드에서 계산하는 방식을 씀
					Magic_List.remove(i);
					Enemy_List.remove(j);
					game_Score+=100;//위에가 일어나면 점수100점 추가
					hit = new Hit(en.x + Enemy_img.getWidth(null) / 2, en.y + Enemy_img.getHeight(null) / 2 , 0);
					//적이 위치해있는 곳의 중심 좌표 x,y 값과 
					//충돌 설정을 받은 값 ( 0 또는 1 )을 받음
					//충돌 설정 값 - 0 : 충돌 , 1 : 공격맞은거 
				}
			}
		}
	}

	public void EnemyProcess(){//적 행동 처리 메소드

		for (int i = 0 ; i < Enemy_List.size() ; ++i ){ 
			en = (Enemy)(Enemy_List.get(i));
			//배열에 디멘터가 생성되어있을 때 해당되는 적을 판별
			en.move(); //디멘터를 이동시킴

			if(Crash(x, y, en.x, en.y, Player_img, Enemy_img)){
				//플레이어와 적의 충돌을 판정하여
				//boolean값을 리턴 받아 true면 아래를 실행함

				player_HP --; //플레이어 체력을 1깍음
				Enemy_List.remove(i); //적을 제거함
				
				hit = new Hit(en.x + Enemy_img.getWidth(null) / 2, en.y + Enemy_img.getHeight(null) / 2, 0 );
				//적이 위치해있는 곳의 중심 좌표 x,y 값과 
				//충돌 설정을 받은 값 ( 0 또는 1 )을 받음
				//충돌 설정 값 - 0 : 충돌 , 1 : 마법 맞았을 때 

				hit = new Hit(x, y, 1 );
				//적이 위치해있는 곳의 중심 좌표 x,y 값과 
				//충돌 설정을 받은 값 ( 0 또는 1 )을 받음
				//충돌 설정 값 - 0 : 충돌 , 1 : 마법 맞았을 때 
			}
		}

		if ( cnt % 100 == 0 ){ //카운트 100회 마다
			en = new Enemy(f_width + 50, 30);//디멘터 생성 
			Enemy_List.add(en);
			en = new Enemy(f_width + 50, 180);
			Enemy_List.add(en); 
			en = new Enemy(f_width + 50, 330);
			Enemy_List.add(en);
			en = new Enemy(f_width + 50, 480);
			Enemy_List.add(en);
			en = new Enemy(f_width + 50, 630);
			Enemy_List.add(en);
		}

	}

	public boolean Crash(int x1, int y1, int x2, int y2, Image img1, Image img2){
		//이미지 변수를 받아 해당 이미지의 넓이, 높이값을 바로 계산
		boolean check = false;

		if ( Math.abs( ( x1 + img1.getWidth(null) / 2 )  
				- ( x2 + img2.getWidth(null) / 2 ))  
				< ( img2.getWidth(null) / 2 + img1.getWidth(null) / 2 )
				&& Math.abs( ( y1 + img1.getHeight(null) / 2 )  
						- ( y2 + img2.getHeight(null) / 2 ))  
				< ( img2.getHeight(null)/2 + img1.getHeight(null)/2 ) ){
			//이미지 넓이, 높이값을 바로 받아 계산함


			check = true;//위 값이 true면 check에 true를 전달함
		}else{ check = false;}

		return check; //check의 값을 메소드에 리턴 시킴
	}

	public void paint(Graphics g){
		buffImage = createImage(f_width, f_height); 
		//더블버퍼링 버퍼 크기를 화면 크기와 같게 설정
		buffg = buffImage.getGraphics();
		//버퍼의 그래픽 객체를 얻기
		update(g);
	}

	public void update(Graphics g){
		Draw_Background();
		Draw_Player();
		Draw_Enemy(); 
		Draw_Magic();
		Draw_StatusText();
		g.drawImage(buffImage, 0, 0, this); 
	}
	public void Draw_Background(){
		//배경 이미지를 그리는 부분
		buffg.clearRect(0, 0, f_width, f_height);
		//화면 지우기 명령은 이제 여기서 실행
		buffg.drawImage(Background_img, 0, 0, this);
	}

	public void Draw_Player(){ //플레이어를 그리는 부분
		buffg.drawImage(Player_img, x, y, this);
	}

	public void Draw_Magic(){//마법 그리는 메소드
		for (int i = 0 ; i < Magic_List.size()  ; ++i){
			//마법 존재 유무확인
			Magic = (Magic) (Magic_List.get(i));
			//마법 위치값 확인
			buffg.drawImage(Magic_img, Magic.x, Magic.y, this); 
			//이 좌표에 마법 그림 이미지 크기 고려해서 마법 좌표 수정
		}
	}

	public void Draw_Enemy(){ //디멘터 그리는 부분
		for (int i = 0 ; i < Enemy_List.size() ; ++i ){
			en = (Enemy)(Enemy_List.get(i));
			buffg.drawImage(Enemy_img, en.x, en.y, this);
			//생성된 적을 판별해서 이미지 그림
		}
	}
	public void Draw_StatusText(){ //상태 체크용  텍스트를 그림

		buffg.setFont(new Font("Defualt", Font.BOLD, 20));
		//폰트 설정을 합니다.  기본폰트, 굵게, 사이즈 20

		buffg.drawString("SCORE : " + game_Score, 1000, 70);
		//좌표 x : 1000, y : 70에 스코어를 표시합니다.

		buffg.drawString("HP : " + player_HP, 1000, 90);
		//좌표 x 1000, y 90에 플레이어 체력을 표시합니다.
		this .setForeground(Color.white);//폰트 색을 흰색으로 합니다.
		if(player_HP<=0) {//체력이 0이 되었을때

			ImageIcon end=new ImageIcon("패배이미지.png");
			JLabel a=new JLabel();//라벨
			JPanel bpanel=new JPanel();//버튼 페널
			JButton strbtn=new JButton("다시하기");//버튼들
			JButton endbtn=new JButton("게임종료");
			JLabel l1=new JLabel("최종 점수:"+game_Score);//점수 라벨
			l1.setFont(new Font("Defualt", Font.BOLD,30));
			a.setIcon (end);
			endpanel.add(a);
			scorepanel.add(l1);
			e.add(BorderLayout.NORTH,scorepanel);
			e.add(BorderLayout.CENTER,endpanel);
			e.add(BorderLayout.SOUTH,bpanel);
			bpanel.add(strbtn);
			bpanel.add(endbtn);
			strbtn.addActionListener(new ActionListener() {//시작 버튼 액션리스너
				public void actionPerformed(ActionEvent e) {
					new Frame();
					f.setVisible(false);
				}
			});
			endbtn.addActionListener(new ActionListener() {//종료버튼 액션 리스너
				public void actionPerformed(ActionEvent e) {
					System.exit(0);
				}
			});

			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.setVisible(false);
			e.setTitle("해리포터");
			e.setResizable(false);
			e.setVisible(true);
			e.setSize(1200,750); 
		}
		if(game_Score>=10000) {//게임 클리어 조건

			ImageIcon end=new ImageIcon("승리 이미지.png");
			JLabel a=new JLabel();
			JPanel bpanel=new JPanel();
			JButton strbtn=new JButton("다시하기");
			JButton endbtn=new JButton("게임종료");
			JLabel l1=new JLabel("YOU WIN!");
			l1.setFont(new Font("Defualt", Font.BOLD,30));

			a.setIcon (end);
			endpanel.add(a);
			scorepanel.add(l1);
			e.add(BorderLayout.NORTH,scorepanel);
			e.add(BorderLayout.CENTER,endpanel);
			e.add(BorderLayout.SOUTH,bpanel);
			bpanel.add(strbtn);
			bpanel.add(endbtn);
			strbtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					new Frame();
					f.setVisible(false);
				}
			});
			endbtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.exit(0);
				}
			});

			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.setResizable(false);
			this.setVisible(false);
			e.setTitle("해리포터");
			e.setVisible(true);
			e.setResizable(false);
			e.setSize(520,750); 
		}
	}

	public void keyPressed(KeyEvent e){//키보드가 눌러졌을때 이벤트 처리함
		switch(e.getKeyCode()){
		case KeyEvent.VK_UP :
			KeyUp = true;
			break;
		case KeyEvent.VK_DOWN :
			KeyDown = true;
			break;
		case KeyEvent.VK_LEFT :
			KeyLeft = true;
			break;
		case KeyEvent.VK_RIGHT :
			KeyRight = true;
			break;

		case KeyEvent.VK_SPACE :
			KeySpace = true;
			break;
		}
	}
	public void keyReleased(KeyEvent e){//눌렸다 때졌을때 처리
		switch(e.getKeyCode()){
		case KeyEvent.VK_UP :
			KeyUp = false;
			break;
		case KeyEvent.VK_DOWN :
			KeyDown = false;
			break;
		case KeyEvent.VK_LEFT :
			KeyLeft = false;
			break;
		case KeyEvent.VK_RIGHT :
			KeyRight = false;
			break;

		case KeyEvent.VK_SPACE :
			KeySpace = false;
			break;

		}
	}
	public void keyTyped(KeyEvent e){}//키보드 쳤을때 이벤트 처리
	public void KeyProcess(){//실제로 캐릭터 움직임 실현을 위해 위에서 받아들인 키를 바탕으로 키 입력시마다 5만큼의 이동을 시킴
		if(KeyUp == true) {
			if( y > 20 ) y -= 5;
			//캐릭터가 보여지는 화면 위로 못 넘어가게 함
		}
		if(KeyDown == true) {
			if( y+ Player_img.getHeight(null) < f_height ) y += 5;
			//캐릭터가 보여지는 화면 아래로 못 넘어가게 함
		}

		if(KeyLeft == true) {
			if ( x > 0 ) x -= 5;
			//캐릭터가 보여지는 화면 왼쪽으로 못 넘어가게 함
		}
		if(KeyRight == true) {
			if ( x + Player_img.getWidth(null) < f_width ) x += 5;
			//캐릭터가 보여지는 화면 오른쪽으로 못 넘어가게 함
		}
	}
}

class Magic{
	int x;
	int y; //편의상 변수 명 변경

	Magic(int x, int y){//마법 좌표를 입력 받는 메소드
		this.x = x; 
		this.y = y;//편의상 변수명 변경
	}
	public void move(){//미사일 이동을 위한 메소드 숫자키우면 빨리짐
		x += 20;
	}
}

class Enemy{ 
	int x;
	int y;

	Enemy(int x, int y){ 
		this.x = x;
		this.y = y;
	}
	public void move(){ //적 이동속도 커질 수록 빨리짐
		x -= 10;
	}
}
class Hit{ 
	// 여러개의 충돌 이미지를 그리기위해 클래스를 추가하여 객체관리 

	int x; //이미지를 그릴 x 좌표
	int y; //이미지를 그릴 y 좌표
	int ex_cnt; //이미지를 순차적으로 그리기 위한 카운터
	int damage; //이미지 종류를 구분하기 위한 변수값

	Hit(int x, int y, int damage){
		this.x = x;
		this.y = y;
		this.damage = damage;
		ex_cnt = 0;
	}
	public void effect(){
		ex_cnt ++; //해당 메소드 호출 시 카운터를 +1 시킴
	}
}

class start extends JFrame{//시작 프레임
	start(){
		JFrame f=new JFrame();
		JPanel panel=new JPanel();
		JPanel bpanel=new JPanel();
		JButton strbtn=new JButton("게임 시작");
		JButton endbtn=new JButton("게임 종료");
		ImageIcon end=new ImageIcon("시작이미지.jpg");
		JLabel a=new JLabel();
		a.setIcon (end);
		panel.add(a);
		f.add(BorderLayout.CENTER,panel);
		f.add(BorderLayout.SOUTH,bpanel);
		bpanel.add(strbtn);
		bpanel.add(endbtn);
		strbtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Frame();
				f.setVisible(false);
			}
		});
		endbtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		f.setTitle("해리포터");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setResizable(false);
		f.setVisible(true);
		f.setSize(1200,750);

	}


}
public class Shooting {
	public static void main(String[] ar){
		new start();

	}
}
