package Window;

import Components.Action;
import Tetriss.AudioPlayer;

import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


import static Tetriss.Configuration.*;


public class Starter {
    static {
        loadConfiguration();//加载配置文件
    }
    private static MainFrame window;//主窗口
    private static TMenu mainMenu;//主菜单
    private static TMenu playMenu;//Play菜单
    private static TInputBox inputBox;//play菜单中的PVP输入面板
    //初始化各组件
    private static void initMainMenu(){
        mainMenu = new TMenu(332,435,true,0,new String[]{ "PLAY", "SETTING", "EXIT"});
        mainMenu.setActions(new Action[]{
                ()->{//PLAY ACTION
                    window.remove(mainMenu);
                    window.removeAllKeyListeners(mainMenu);
                    window.add(playMenu);
                    window.addAllKeyListeners(playMenu);
                    window.repaint();
                },
                ()-> {//SETTING ACTION
                    new SettingWindow().setVisible(true);
                },
                ()-> {
                    window.dispose();
                    AudioPlayer.close();
                    System.exit(0);
                }//EXIT ACTION
        });
    }
    private static void initPlayMenu(){
        playMenu = new TMenu(332,435,true,0,new String[]{ "SINGLE", "PVP", "BACK"});
        playMenu.setActions(new Action[]{
                ()->{//选择single,单人游戏
                    SinglePlayWindow singlePlayWindow = new SinglePlayWindow();
                    singlePlayWindow.start();
                    singlePlayWindow.setWindowCloseAction(() -> {
                        window.addAllKeyListeners(playMenu);
                        window.setVisible(true);
                    });
                    window.removeAllKeyListeners(playMenu);
                    window.setVisible(false);

                },
                ()->{//选择PVP,双人PK
                    window.remove(playMenu);
                    window.removeAllKeyListeners(playMenu);
                    window.add(inputBox);
                    window.addAllKeyListeners(inputBox);
                    window.repaint();

                },
                ()->{//选择back，返回上一级
                    window.remove(playMenu);
                    window.removeAllKeyListeners(playMenu);
                    window.add(mainMenu);
                    window.addAllKeyListeners(mainMenu);
                    window.repaint();
                }
        });
    }
    private static void initInputBox(){
        inputBox = new TInputBox(332,435,"127.0.0.1",new String[]{"CONNECT","BACK"});
        inputBox.setActions(new Action[]{
                ()->{
                    if("127.0.0.1".equals(inputBox.getContent())){//输入本机地址，线下PK
                        OffLinePVPWindow offLinePVPWindow = new OffLinePVPWindow();
                        offLinePVPWindow.start();
                        offLinePVPWindow.setWindowCloseAction(()->{
                            window.setVisible(true);
                            window.addAllKeyListeners(inputBox);
                        });
                        window.setVisible(false);
                        window.removeAllKeyListeners(inputBox);

                    }else{//否则向指定IP发送PK请求
                        new Thread(() -> {
                            try {
                                sendRequest(inputBox.getContent());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }).start();
                    }
                },
                ()->{//返回上级
                    window.remove(inputBox);
                    window.removeAllKeyListeners(inputBox);
                    window.add(playMenu);
                    window.addAllKeyListeners(playMenu);
                    window.repaint();
                }
        });
    }
    //开始监听请求
    private static void startListenRequest(){
        new Thread(() -> {//在子线程中监听
            ServerSocket serverSocket = null;
            try {
                //服务端用20023端口监听
                serverSocket = new ServerSocket(20023);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            while (true) {
                Socket connectionSocket = null;
                try {
                    connectionSocket = serverSocket.accept();//如果监听到了
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                //创建socket
                Socket finalConnectionSocket = connectionSocket;
                new Thread(() -> {//在新的子线程中处理
                    try {
                        //获取输入流（从客户端到服务端）
                        BufferedReader inFromClient = new BufferedReader(new InputStreamReader(finalConnectionSocket.getInputStream()));
                        //获取输出流（从服务端到客户端）
                        DataOutputStream outToClient = new DataOutputStream(finalConnectionSocket.getOutputStream());
                        //读取客户端发送过来的端口（客户端告诉服务端自己将使用哪个端口接收游戏的进度）
                        int clientPort = Integer.parseInt(inFromClient.readLine());
                        //从socket中获取客户端的IP
                        String clientIP = finalConnectionSocket.getInetAddress().getHostAddress();
                        //创建对话框
                        TDialog dialog = new TDialog("Received an invitation from\n" + clientIP + "\nAccept it?", new String[]{"ACCEPT", "REJECT"});
                        //设置对话框的响应事件
                        dialog.setActions(new Action[]{
                                ()->{//如果接受
                                    int serverUDPPort = 20025;
                                    outToClient.writeBytes(serverUDPPort+"\n");//服务端告诉客户端自己将使用哪个端口接收游戏的进度
                                    //根据客户端IP、端口和服务端的端口创建线上对战窗口
                                    OnLinePVPWindow onLinePVPWindow = new OnLinePVPWindow(clientIP,clientPort,serverUDPPort);
                                    //服务端接收客户端发送的方块序列
                                    onLinePVPWindow.getSpecifiedTetris().receiveBlocks(inFromClient);
                                    //服务端向客户端发送方块序列
                                    onLinePVPWindow.getSpecifiedTetris().sendBlocks(outToClient);
                                    //服务端发送序列结束后关闭socket的输出流
                                    finalConnectionSocket.shutdownOutput();
                                    //开始混合己方和对方的方块序列
                                    onLinePVPWindow.getSpecifiedTetris().mixBlocks();
                                    //开始游戏
                                    onLinePVPWindow.start();
                                    //设置对战窗口的关闭响应行为
                                    onLinePVPWindow.setWindowCloseAction(()->{
                                        //关闭对战窗口后重新显示主窗口
                                        window.setVisible(true);
                                    });
                                    //关闭socket
                                    finalConnectionSocket.close();
                                    //销毁对话框
                                    dialog.dispose();
                                    //隐藏主窗口
                                    window.setVisible(false);
                                },
                                ()->{//如果拒绝
                                    //发送“0\n”
                                    outToClient.writeBytes(0+"\n");
                                    //销毁对话框
                                    dialog.dispose();
                                }
                        });
                        //显示对话框
                        dialog.setVisible(true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }).start();
    }
    //发送请求
    private static void sendRequest(String serverIP) throws IOException {
        String serverUDPPort;//服务器用于接收游戏进度的端口
        Socket clientSocket = new Socket(serverIP, 20023);//尝试连接服务器的20023端口
        //获取输出流（从客户端到服务器）
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        //获取输入流（从服务端到客户端）
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        //客户端将自己用于接收游戏进度的端口告诉服务端
        int clientUDPPort = 20024;
        outToServer.writeBytes(clientUDPPort + "\n");
        //客户端从服务端接收请求结果
        serverUDPPort = inFromServer.readLine();
        if(!serverUDPPort.equals("0")){//如果服务端接受
            //根据服务端IP、端口和客户端的端口创建线上对战窗口
            OnLinePVPWindow onLinePVPWindow = new OnLinePVPWindow(serverIP,Integer.parseInt(serverUDPPort),clientUDPPort);
            //向服务端发送方块序列
            onLinePVPWindow.getSpecifiedTetris().sendBlocks(outToServer);
            //关闭输出流
            clientSocket.shutdownOutput();
            //客户端接受服务端的方块序列
            onLinePVPWindow.getSpecifiedTetris().receiveBlocks(inFromServer);
            //混合己方和对方的方块
            onLinePVPWindow.getSpecifiedTetris().mixBlocks();
            //开始游戏
            onLinePVPWindow.start();
            //设置对战窗口的关闭行为
            onLinePVPWindow.setWindowCloseAction(()->{
                window.setVisible(true);
            });
            //隐藏主窗口
            window.setVisible(false);
        }
        //关闭socket
        clientSocket.close();
    }
    public static void main(String[] args) throws IOException {
        //开始监听请求
        startListenRequest();
        //预播放音频
        AudioPlayer.prePlay();
        //创建主窗口
        window  = new MainFrame();
        //设置尺寸
        Dimension dimension =  new Dimension(MAINWINDOW_WIDTH,MAINWINDOW_HEIGHT);
        //设置居中
        Dimension screen =  Toolkit.getDefaultToolkit().getScreenSize();
        window.setBounds((screen.width-MAINWINDOW_WIDTH)/2,(screen.height-MAINWINDOW_HEIGHT)/2,dimension.width,dimension.height);
        //无修饰（无关闭、缩小按钮等）
        window.setUndecorated(true);
        //设置透明背景
        window.setBackground(new Color(0,0,0,0));
        //绝对布局
        window.setLayout(null);
        //初始化组件
        initMainMenu();
        initPlayMenu();
        initInputBox();
        //添加主菜单和监听器
        window.add(mainMenu);
        window.addAllKeyListeners(mainMenu);
        //显示窗口
        window.setVisible(true);
    }
}
