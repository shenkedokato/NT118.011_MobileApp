package com.example.myapplication;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.CountDownTimer;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.database.DatabaseReference;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class GeneralFunc {
    //string format mặc định là uft-8

    //Chuyển string thành base 64 string (lưu ý: nếu chuyển byte[] thành string rồi đưa vào hàm này
    // thì khi chuyển lại base 64 thành string sẽ có tỉ lệ sai)
    public static String str2Base64(String string){
        Base64.Encoder encoder=Base64.getEncoder();
        byte[] bytes= encoder.encode(string.getBytes());
        return new String(bytes);
    }

    //Chuyển base 64 thành string
    public static String base64ToStr(String string){
        Base64.Decoder decoder=Base64.getDecoder();
        byte[] bytes= decoder.decode(string.getBytes());
        return new String(bytes);
    }

    //Nén img thành base 64 string với input là bitmap và quality
    public static String zipImg2Base64(Bitmap bitmap, int quality){
        try(ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
            DeflaterOutputStream deflaterOutputStream=new DeflaterOutputStream(byteArrayOutputStream)){

            //Bitmap bitmap= BitmapFactory.decodeFile(imgPath,options);
            ByteArrayOutputStream byteArrayOutputStream1=new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality,byteArrayOutputStream1);
            byte[] bytes=byteArrayOutputStream1.toByteArray();
            byteArrayOutputStream1.close();

            deflaterOutputStream.write(bytes);
            deflaterOutputStream.close();

            String ret= new String(Base64.getEncoder().encode(byteArrayOutputStream.toByteArray()));
            byteArrayOutputStream.close();
            return ret;
        } catch (IOException e){
            return "";
        }
    }

        //Nén ảnh đại diện mặc định thành base 64
    public static String defaultImg2Base64(Context context){
        try(ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
            DeflaterOutputStream deflaterOutputStream=new
                    DeflaterOutputStream(byteArrayOutputStream)){

            Bitmap bitmap= BitmapFactory.decodeResource(context.getResources(),R.drawable.user);
            ByteArrayOutputStream byteArrayOutputStream1=new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100,byteArrayOutputStream1);
            byte[] bytes=byteArrayOutputStream1.toByteArray();
            byteArrayOutputStream1.close();

            deflaterOutputStream.write(bytes);
            deflaterOutputStream.close();

            String ret= new String(Base64.getEncoder().encode(byteArrayOutputStream.toByteArray()));
            byteArrayOutputStream.close();
            return ret;
        } catch (IOException e){
            return "";
        }
    }

    //Giải nén từ base 64
    public static Bitmap unzipBase64ToImg(String string){
        byte[] bytes=Base64.getDecoder().decode(string.getBytes());
        try(ByteArrayInputStream byteArrayInputStream=new ByteArrayInputStream(bytes);
                InflaterInputStream inflaterInputStream = new InflaterInputStream(byteArrayInputStream)){

            ByteArrayOutputStream byteArrayOutputStream1=new ByteArrayOutputStream();
            int read;
            while ((read = inflaterInputStream.read()) != -1) {
                byteArrayOutputStream1.write(read);
            }

            inflaterInputStream.close();
            byteArrayInputStream.close();
            byte[] bytes1=byteArrayOutputStream1.toByteArray();
            byteArrayOutputStream1.close();

            return BitmapFactory.decodeByteArray(bytes1,0,bytes1.length);
        } catch (IOException e){
            return null;
        }
    }

    //Hàm xử lý ẩn hiện pass
    public static void showHidPass(ImageView seePass, EditText et_pass){
        if(et_pass.getInputType()==131073){ //id của normal text
            et_pass.setInputType(129); //id của password text
            seePass.setImageResource(R.drawable.eye_close);
        }else {
            et_pass.setInputType(131073);
            seePass.setImageResource(R.drawable.eye);
        }
    }

    //Chạy count down
    public static void startTimer(Context context, TextView tv_clickable, TextView tv_cd, int second){
        tv_clickable.setTextColor(context.getColor(R.color.black));
        tv_clickable.setClickable(false);
        CountDownTimer countDownTimer=new CountDownTimer(second*1000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tv_cd.setText(String.valueOf((int)(millisUntilFinished/1000)));
            }

            @Override
            public void onFinish() {
                tv_clickable.setTextColor(context.getColor(R.color.orange_brown));
                tv_clickable.setClickable(true);
                tv_cd.setText("");
            }
        }.start();
    }

    //Yêu cầu cấp quyền
    public static void askPermission(Activity activity, String permission){
        if (ContextCompat.checkSelfPermission(activity, permission) !=
                PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            ActivityCompat.requestPermissions(activity, new String[]{permission}, 1);
        }
    }

    //Kiểm tra internet
    public static boolean hasInternet(Activity activity){
        ConnectivityManager connectivityManager=
                (ConnectivityManager) activity.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=connectivityManager!=null?
                connectivityManager.getActiveNetworkInfo():null;
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    //Tạo item pic
    public static LinearLayout itemPic(Context context, objectPic pic){
        LinearLayout linearLayout=(LinearLayout) ((LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.item_pic,null);
        linearLayout.setId(LinearLayout.generateViewId());
        ConstraintLayout.LayoutParams layoutParams=new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        linearLayout.setLayoutParams(layoutParams);

        CardView cardView=(CardView) linearLayout.getChildAt(0);

        ImageView imageView = (ImageView) cardView.getChildAt(0);
        imageView.setImageBitmap(unzipBase64ToImg(pic.getData()));
        imageView.setTag(pic);

        linearLayout.setTag((float)(imageView.getDrawable().getIntrinsicHeight()/
                imageView.getDrawable().getIntrinsicWidth()));

        return linearLayout;
    }

    //Tạo item user
    public static LinearLayout itemUser(Context context, objectUser user){
        LinearLayout linearLayout=(LinearLayout)((LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.item_user,null);
        linearLayout.setId(LinearLayout.generateViewId());
        ConstraintLayout.LayoutParams layoutParams=new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        linearLayout.setLayoutParams(layoutParams);

        CardView cardView=(CardView) linearLayout.getChildAt(0);
        ImageView imageView=(ImageView)cardView.getChildAt(0);

        LinearLayout linearLayout1=(LinearLayout) linearLayout.getChildAt(1);
        TextView name=(TextView) linearLayout1.getChildAt(0),
                email=(TextView)linearLayout1.getChildAt(1);

        imageView.setImageBitmap(unzipBase64ToImg(user.getDataUserPic()));
        imageView.setTag(user);
        name.setText(user.getUsername());
        email.setText(base64ToStr(user.getB64Email()));

        return linearLayout;
    }

    //Đưa item ảnh vào giao diện
    public static void items2Layout(ConstraintLayout constraintLayout,
                                    ArrayList<LinearLayout> linearLayoutArrayList,
                                    View.OnClickListener onItemClickListener){
        float height0=0, height1=0;
        int idLast0= ConstraintSet.PARENT_ID, idLast1=ConstraintSet.PARENT_ID;

        for(int i=0;i<linearLayoutArrayList.size();i++){
            //Cập nhật tổng chiều cao
            if(i>0) {
                if(height0<=height1){
                    height0+=(float)constraintLayout.getChildAt(i-1).getTag();
                }else {
                    height1+=(float)constraintLayout.getChildAt(i-1).getTag();
                }
            }

            LinearLayout linearLayout=linearLayoutArrayList.get(i);

            //Sự kiện click ảnh
            linearLayout.setOnClickListener(onItemClickListener);

            constraintLayout.addView(linearLayout);

            ConstraintSet constraintSet=new ConstraintSet();
            constraintSet.clone(constraintLayout);

            //Thiết lập các ràng buộc
            if(height0<=height1){
                constraintSet.connect(linearLayout.getId(), ConstraintSet.START,
                        ConstraintSet.PARENT_ID, ConstraintSet.START);
            }else {
                constraintSet.connect(linearLayout.getId(), ConstraintSet.END,
                        ConstraintSet.PARENT_ID, ConstraintSet.END);
                constraintSet.connect(linearLayout.getId(), ConstraintSet.START,
                        idLast0, ConstraintSet.END);

                constraintSet.connect(idLast0, ConstraintSet.END,
                        linearLayout.getId(), ConstraintSet.START);
            }

            if(i<2){
                constraintSet.connect(linearLayout.getId(), ConstraintSet.TOP,
                        ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            }else {
                if(height0<=height1){
                    constraintSet.connect(linearLayout.getId(), ConstraintSet.END,
                            idLast1, ConstraintSet.START);

                    constraintSet.connect(linearLayout.getId(), ConstraintSet.TOP,
                            idLast0, ConstraintSet.BOTTOM);
                } else{
                    constraintSet.connect(linearLayout.getId(), ConstraintSet.TOP,
                            idLast1, ConstraintSet.BOTTOM);
                }
            }

            if(height0<=height1){
                idLast0=linearLayout.getId();
            }else {
                idLast1=linearLayout.getId();
            }

            constraintSet.applyTo(constraintLayout);
        }

        constraintLayout.setTag(R.id.height0,height0);
        constraintLayout.setTag(R.id.height1,height1);

    }

    //Đưa item user vào giao diện
    public static void itemsUser2Layout(ConstraintLayout constraintLayout,
                                    ArrayList<LinearLayout> linearLayoutArrayList,
                                    View.OnClickListener onItemClickListener){

        for(int i=0;i<linearLayoutArrayList.size();i++){
            LinearLayout linearLayout=linearLayoutArrayList.get(i);

            //Sự kiện click tk
            linearLayout.setOnClickListener(onItemClickListener);

            constraintLayout.addView(linearLayout);

            ConstraintSet constraintSet=new ConstraintSet();
            constraintSet.clone(constraintLayout);


            //Thiết lập các ràng buộc
            constraintSet.connect(linearLayout.getId(), ConstraintSet.START,
                    ConstraintSet.PARENT_ID, ConstraintSet.START);

            if(i<1){
                constraintSet.connect(linearLayout.getId(), ConstraintSet.TOP,
                        ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            }else {
                constraintSet.connect(linearLayout.getId(), ConstraintSet.TOP,
                        linearLayoutArrayList.get(i-1).getId(), ConstraintSet.BOTTOM);
            }

            constraintSet.applyTo(constraintLayout);
        }

    }

}
