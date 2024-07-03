package Tetriss;

import javax.sound.sampled.*;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;

import static Tetriss.Configuration.RESOURCES_PATH;

public class AudioPlayer{
    private static SourceDataLine line;
    private static byte[] audioData;
    private static int length;
    private static BooleanControl muteControl = null;
    private static AudioInputStream audioStream = null;
    static {
        File audioFile = new File(RESOURCES_PATH + "fixedSoundEffect.wav");//读取音频文件
        try {
            audioStream = AudioSystem.getAudioInputStream(audioFile);//获取输入流
            AudioFormat format = audioStream.getFormat();// 获取音频格式
            length = (int) (audioStream.getFrameLength() * format.getFrameSize());// 获取音频数据的长度
            audioData = new byte[length];// 创建一个字节数组来存储音频数据
            DataInputStream is = new DataInputStream(audioStream);// 读取音频数据到字节数组中
            is.readFully(audioData);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);//获取一个SourceDataLine对象并打开
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
    public static void play(){
        if(Configuration.hasSound){//如果在设置中开启了音效
            muteControl.setValue(false);//设置非静音
            line.flush();//刷新line
            line.start();//重新播放
            line.write(audioData, 0, length);
        }
    }
    public static void prePlay(){//预播放
        muteControl = (BooleanControl) line.getControl(BooleanControl.Type.MUTE);
        muteControl.setValue(true);
        line.start();
        line.write(audioData, 0, length);
    }
    public static void close(){//关闭
        line.drain();
        line.close();
        try {
            audioStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}