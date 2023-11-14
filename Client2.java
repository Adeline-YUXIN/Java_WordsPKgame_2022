import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import  java.net.*;
import java.io.*;

public class Client2 extends JFrame implements Runnable,ActionListener {
    Color c = new Color(254, 170, 151);                         //设置背景颜色
    Font f1 = new Font("微软雅黑", Font.CENTER_BASELINE, 20);//设置字体样式
    Font f2 = new Font("宋体", Font.CENTER_BASELINE, 16);
    Font f3 = new Font("微软雅黑", Font.BOLD, 15);
    JPanel jp = new JPanel();                                         //创建布局
    JLabel jl1 = new JLabel("初始生命值10");
    JLabel jl2 = new JLabel("正在为您匹配对手…");
    JTextField jtf = new JTextField("请输入你的答案");
    Dimension dm_jtf = new Dimension(10, 30);
    String nickname;

    JLabel movechar = new JLabel();//掉落的单词

    String Wordtofile=null;

    int X = 140;//题目坐标
    int Y = 10;

    private BufferedReader br = null;//用于接收服务器消息
    private PrintStream ps = null;//用于发送消息给服务器
    String[] str=null;
    Thread1 th1 =null;//使单词掉落的线程1

    public Client2() throws Exception { //图形界面
        this.setSize(650, 600);                       //设置界面大小
        this.setLocation(0,80);                         //设置界面位置
        this.add(jp);                                           //布局实体与界面关联起来
        jp.setBackground(c);                                    //设置背景颜色
        jp.setLayout(new BorderLayout());
        jl1.setFont(f2);
        jl1.setForeground(new Color(0, 0, 0));
        jp.add(jl1, BorderLayout.WEST);
        jl2.setFont(f1);
        jl2.setForeground(new Color(165, 165, 165));
        jp.add(jl2, BorderLayout.EAST);
        jtf.setPreferredSize(dm_jtf);
        jtf.addActionListener(this);
        jp.add(jtf, BorderLayout.SOUTH);
        jp.add(movechar, BorderLayout.NORTH);
        movechar.setFont(f3);
        movechar.setSize(20,0);
        movechar.setForeground(new Color(255, 255, 255));

        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);//窗体可见，

        Socket s = new Socket("100.126.51.102", 9999);
        br = new BufferedReader(new InputStreamReader(s.getInputStream()));//用于接收服务器消息
        ps = new PrintStream(s.getOutputStream());//用于发送消息给服务器
        nickname = JOptionPane.showInputDialog("请输入昵称->开始游戏");//一开始的消息提示窗
        this.setTitle("CET6单词PK 玩家:" + nickname);
        ps.println("NICK:"+nickname);//向服务器发送nickname

        new Thread(this).start();//接收服务器消息的线程
        th1 = new Thread1();//使单词掉落的线程
        th1.start();
        ps.println("start");
    }

    public void run() {
        while (true) {
            try {
                String msg = br.readLine();//读服务器的消息
                if(msg.startsWith("生命值:"))
                {
                    jl1.setText("当前"+msg);
                }
                else if(msg.equals("对方回答正确")){
                    th1.suspend();
                    JOptionPane.showMessageDialog(this,"对方回答正确,正确答案为\n"+str[0]+"  "+str[1],"玩家"+nickname,JOptionPane.PLAIN_MESSAGE);
                    File f1 = new File("Fail.txt");
                    FileOutputStream fos1 = new FileOutputStream(f1, true);
                    fos1.write(Wordtofile.getBytes());
                    fos1.write("（未答）\n".getBytes());
                    fos1.close();

                    ps.println("start");
                }
                else if(msg.equals("对方回答错误")){
                    th1.suspend();
                    JOptionPane.showMessageDialog(this,"对方回答错误,正确答案为\n"+str[0]+"  "+str[1],"玩家"+nickname,JOptionPane.PLAIN_MESSAGE);
                    File f1 = new File("Fail.txt");
                    FileOutputStream fos1 = new FileOutputStream(f1, true);
                    fos1.write(Wordtofile.getBytes());

                    fos1.write(("（未答）\n").getBytes());
                    fos1.close();

                    ps.println("start");
                }
                else if(msg.equals("lose")){

                    JOptionPane.showMessageDialog(this,"您输了,再接再厉","玩家"+nickname,JOptionPane.PLAIN_MESSAGE);
                    System.exit(1); }
                else if(msg.equals("win")){

                    JOptionPane.showMessageDialog(this,"您赢得了比赛！","玩家"+nickname,JOptionPane.PLAIN_MESSAGE);
                    System.exit(1); }
                else{
                    th1.resume();
                    Wordtofile=msg;
                    str=msg.split(":");
                    movechar.setText(str[1]);
                    String L="     提示一下: "+str[0].charAt(0)+""+str[0].charAt(1)+"______";
                    jl2.setText(L);
                    Y=0;}
            } catch (Exception e) {
            }
        }
    }

    public void actionPerformed(ActionEvent e) { //监听器
        try{
            String a = jtf.getText();
            if(a.equals(str[0]))
            {
                th1.suspend();
                File f1=new File("Success.txt");
                FileOutputStream fos1=new FileOutputStream(f1,true);
                fos1.write(Wordtofile.getBytes());
                fos1.write("\n".getBytes());
                fos1.close();

                ps.println("true");
                JOptionPane.showMessageDialog(this,"恭喜你回答正确！分数+1","玩家"+nickname,JOptionPane.PLAIN_MESSAGE);
                ps.println("start");

            }
            else{
                th1.suspend();
                File f1=new File("Fail.txt");
                FileOutputStream fos1=new FileOutputStream(f1,true);
                fos1.write(Wordtofile.getBytes());
                fos1.write("（答错）\n".getBytes());
                fos1.close();

                ps.println("false");
                JOptionPane.showMessageDialog(this,"回答错误！分数-2，正确答案为\n"+str[0]+"  "+str[1],"玩家"+nickname,JOptionPane.PLAIN_MESSAGE);
                ps.println("start");

            }
            jtf.setText("");

        }catch ( Exception ex){}}

    class Thread1 extends Thread {
        public void run() {
            while (true) {
                try {
                    Thread.sleep(50);
                    if (Y < 510) {
                        Y += 2;
                        movechar.setLocation(X, Y);

                    } else if (Y == 510) {
                        JOptionPane.showMessageDialog(null, "回答超时！分数-1，正确答案为\n" + str[0] + "  " + str[1],"玩家"+nickname,JOptionPane.PLAIN_MESSAGE);
                        File f1 = new File("Fail.txt");
                        FileOutputStream fos1 = new FileOutputStream(f1, true);
                        fos1.write(Wordtofile.getBytes());
                        fos1.write("（未答）\n".getBytes());
                        fos1.close();

                        ps.println("over");
                        ps.println("start");
                        Y = 1000;//跳出执行

                    }
                }  catch (Exception e) {
                }
            }
        }


    }
    public static void main(String[] args) throws Exception {
        Client2 C1=new Client2();
    }
}
