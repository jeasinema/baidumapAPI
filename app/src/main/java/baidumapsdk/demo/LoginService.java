package baidumapsdk.demo;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gzy on 2015/8/20.
 */
public class LoginService {

    public static boolean saveUserInfo(Context context,String username,String password){
        try{
            File file =new File(context.getFilesDir(),"userInfo.txt");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write((username+"##"+password).getBytes());
            fos.close();
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static Map<String,String> getSavedUserInfo(Context context){
        try {
            File file = new File(context.getFilesDir(),"userInfo.txt");
            FileInputStream fis = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String str = br.readLine();
            String[] infos =str.split("##");
            Map<String,String> map =new HashMap<String,String>();
            map.put("username",infos[0]);
            map.put("password",infos[1]);
            return map;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
