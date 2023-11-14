import java.net.*;
import java.io.*;
import java.util.*;

class Server implements Runnable {
    private ServerSocket ss = null;
    File file = new File("CET6words.txt");
    LinkedList<String> LL=new LinkedList<String>();          //单词链表
    ArrayList<ChatThread> users = new ArrayList<ChatThread>();   //用户链表/线程链表（一对一关系）
    int count=0;//用于判断用户配对成功
    public Server() throws Exception {
        ss = new ServerSocket(9999);
        new Thread(this).start();
        Scanner sc = new Scanner(file);//单词录入
        while (sc.hasNext()) { //hasNext()判断扫描器中当前扫描位置后是否还存在下一段
            String word = sc.next();//查找并返回来自此扫描器的下一个完整标记
            LL.add(word);//单词链表新增单词
        }
    }

    public void run() { //客户端连接
        while (true) {
            try {
                Socket s = ss.accept();
                ChatThread ct = new ChatThread(s);
                users.add(ct);//用户链表新增
                ct.start();
            } catch (Exception ex) {}
        }
    }

    class ChatThread extends Thread { //每个客户端一个线程
        BufferedReader br = null;
        PrintStream ps = null;
        String nickname = null;
        int Z=(int)(Math.random()*1800);//产生0-49的随机数
        int mark=10;
        ChatThread(Socket s) throws Exception {
            br = new BufferedReader(new InputStreamReader(s.getInputStream()));//接收socket发来的消息
            ps = new PrintStream(s.getOutputStream());//向客户端要发送的消息
        }

        public void run() {//接收消息
            while (true) {
                try {
                    String msg = br.readLine();
                    if (msg.equals("true")) {
                        mark+=1;
                        for(ChatThread ct : users){//遍历用户链表
                            if(ct.nickname.equals(this.nickname)){
                                continue;//如果遍历到的用户名与发送“true”消息的用户名一致，则跳过
                            }
                            ct.ps.println("对方回答正确");
                        }
                    }else if(msg.equals("over")) {
                        mark-=1;
                    }else if(msg.startsWith("NICK")){
                        String[] strs = msg.split(":");
                        nickname = strs[1];
                    }else if(msg.equals("start")) {
                        count++;}
                    else
                    {
                        mark-=2;
                        for(ChatThread ct : users){
                            if(ct.nickname.equals(this.nickname)){
                                continue;
                            }
                            ct.ps.println("对方回答错误");
                        }
                    }
                    ps.println("生命值:"+mark);
                    if(mark<=0)
                    {
                        ps.println("lose");
                        for(ChatThread ct : users){
                            if(ct.nickname.equals(this.nickname)){
                                continue;
                            }
                            ct.ps.println("win");
                        }
                    }
                    if(count==2){
                        Z=(int)(Math.random()*1800);
                        for(ChatThread ct : users) {
                            ct.ps.println(LL.get(Z));}
                        count=0;}
                } catch (Exception e) {
                }
            }
        }

    }
    public static void main(String[] args) throws Exception {
        new Server();
    }
}