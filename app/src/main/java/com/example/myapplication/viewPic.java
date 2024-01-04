package com.example.myapplication;

import static android.content.Intent.makeMainSelectorActivity;
import static android.view.View.GONE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.databinding.ActivityUpdateImgBinding;
import com.example.myapplication.databinding.ActivityViewPicBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class viewPic extends AppCompatActivity {

    objectUser user;
    objectPic pic;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference mDB;
    ActivityViewPicBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityViewPicBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent=getIntent();
        if(intent!=null){
            binding.pbViewPic.setVisibility(View.VISIBLE);
            //Kết nối FB Realtime DB
            firebaseDatabase= FirebaseDatabase.getInstance(
                    "https://surpic-324b6-default-rtdb.asia-southeast1.firebasedatabase.app");
            mDB=firebaseDatabase.getReference();

            //Nhận dữ liệu
            user = intent.getParcelableExtra("user");
            pic = intent.getParcelableExtra("viewPic");

            ImageView vpImg = binding.ivViewPicImg;

            //Cập nhật giao diện
            if(pic==null){
                pic=intent.getParcelableExtra("viewPicMin");

                mDB.child(pic.getB64EmailOwner()).child("pics").child(pic.getKey()).get()
                        .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if(task.isSuccessful()){
                            vpImg.setTag(task.getResult().child("data").getValue());
                            vpImg.setImageBitmap(GeneralFunc.unzipBase64ToImg(vpImg.getTag()
                                    .toString()));
                        }
                    }
                });
            }else{
                vpImg.setImageBitmap(GeneralFunc.unzipBase64ToImg(pic.getData()));
                vpImg.setTag(pic);
            }

            mDB.child(pic.getB64EmailOwner()).child(getString(R.string.profile_pic)).get()
                    .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if(task.isSuccessful()){
                                binding.ivViewPicProfilePic.setImageBitmap(GeneralFunc.unzipBase64ToImg(
                                        task.getResult().getValue().toString()));
                                binding.ivViewPicProfilePic.setTag(task.getResult().getValue().toString());
                                binding.pbViewPic.setVisibility(GONE);
                            }
                        }
                    });
            mDB.child(pic.getB64EmailOwner()).child("username").get()
                    .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if(task.isSuccessful()){
                                binding.tvViewPicUsername.setText(task.getResult().getValue()
                                        .toString());
                            }
                        }
                    });

            if(pic.getName().length()!=0 || pic.getName().equals("null")){
                binding.tvViewPicNamePic.setText("Tên ảnh: "+pic.getName());
            }
            if(pic.getTags()[0].length() !=0 || pic.getTags()[0].equals("null")){
                binding.tvViewPicTagsPic.setText("Tags: "+pic.getStrHashtags());
            }

            //Cập nhật trạng thái theo dõi
            mDB.child(user.getB64Email()).child("follow").child(pic.getB64EmailOwner())
                    .get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if(task.isSuccessful()){
                                if(task.getResult().getValue()!=null){
                                    binding.bViewPicFollow.setText("Bỏ theo dõi");
                                    binding.bViewPicFollow.setBackgroundTintList(ContextCompat
                                            .getColorStateList(viewPic.this,
                                                    R.color.red));
                                    binding.bViewPicFollow.setTag("1");
                                }
                            }
                        }
                    });

            //Cập nhật trạng thái yêu thích
            mDB.child(user.getB64Email()).child("love").child(pic.getB64EmailOwner())
                    .child(pic.getKey()).get().addOnCompleteListener(
                            new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    if(task.isSuccessful()){
                                        if(task.getResult().getValue()!=null){
                                            binding.ibViewPicLove.setImageDrawable(getDrawable(
                                                    R.drawable.baseline_favorite_24));
                                            binding.ibViewPicLove.setTag("1");
                                        }
                                    }
                                }
                            });


            //Cập nhật số người theo dõi và số người love
            mDB.child(pic.getB64EmailOwner()).child("follower").get().addOnCompleteListener(
                    new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if(task.isSuccessful()){
                                binding.tvViewPicNumberOfFollower.setText(String.valueOf(task.getResult()
                                        .getChildrenCount())+" người theo dõi");
                            }
                        }
                    });
            mDB.child(pic.getB64EmailOwner()).child("pics").child(pic.getKey())
                    .child("lover").get().addOnCompleteListener(
                            new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    if(task.isSuccessful()){
                                        binding.tvViewPicNumberOfLove.setText(String.valueOf(task.getResult()
                                                .getChildrenCount()));
                                    }
                                }
                            });

            //Nút quay về
            binding.fabViewPicBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            //Nút more của view pic
            FloatingActionButton floatingActionButton = binding.fabViewPicMore;
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Khởi tạo menu setting
                    PopupMenu popupMenu=new PopupMenu(viewPic.this,floatingActionButton);

                    //Nhận item
                    popupMenu.getMenuInflater().inflate(R.menu.view_pic_more_menu,popupMenu.getMenu());

                    String string=binding.bViewPicFollow.getTag().toString();
                    boolean isFollowed=!binding.bViewPicFollow.getTag().toString().equals("0");
                    if(!isFollowed){
                        popupMenu.getMenu().getItem(0).setTitle("Theo dõi "+user.getUsername());
                    } else {
                        popupMenu.getMenu().getItem(0).setTitle("Bỏ theo dõi "+user.getUsername());
                    }

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            //Lựa chọn (bỏ) theo dõi
                            if(item.getItemId()==R.id.vp_more_i_follow){
                                if(!isFollowed){
                                    mDB.child(pic.getB64EmailOwner()).child("follower")
                                            .child(user.getB64Email()).setValue(true);
                                    mDB.child(user.getB64Email()).child("follow")
                                            .child(pic.getB64EmailOwner()).setValue(true);

                                    binding.bViewPicFollow.setText("Bỏ theo dõi");
                                    binding.bViewPicFollow.setBackgroundTintList(ContextCompat
                                            .getColorStateList(viewPic.this,
                                                    R.color.red));
                                    binding.bViewPicFollow.setTag("1");
                                }else {
                                    mDB.child(pic.getB64EmailOwner()).child("follower")
                                            .child(user.getB64Email()).removeValue();
                                    mDB.child(user.getB64Email()).child("follow")
                                            .child(pic.getB64EmailOwner()).removeValue();

                                    binding.bViewPicFollow.setText("Theo dõi");
                                    binding.bViewPicFollow.setBackgroundTintList(ContextCompat
                                            .getColorStateList(viewPic.this,
                                                    R.color.gray));
                                    binding.bViewPicFollow.setTag("0");
                                }

                                //Cập thật số người theo dõi
                                mDB.child(pic.getB64EmailOwner()).child("follower").get().addOnCompleteListener(
                                        new OnCompleteListener<DataSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                if(task.isSuccessful()){
                                                    binding.tvViewPicNumberOfFollower.setText(String.valueOf(task.getResult()
                                                            .getChildrenCount())+" người theo dõi");
                                                }
                                            }
                                        });

                            }

                            //Lựa chọn tải về
                            if(item.getItemId()==R.id.vp_more_i_download){
                                mDB.child(pic.getB64EmailOwner()).child("pics")
                                        .child(pic.getKey()).child("full").get()
                                        .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                        File directory = new File(Environment.getExternalStorageDirectory(),
                                                Environment.DIRECTORY_PICTURES+"/surpic");
                                        if (!directory.exists()) {
                                            directory.mkdirs();
                                        }

                                        //Tự động tạo file dựa vào thời gian
                                        String timeStamp = DateFormat.format("yyyyMMdd_HHmmss",
                                                new Date()).toString();
                                        String fileName = "image_" + timeStamp + ".jpeg";

                                        File file = new File(directory, fileName);

                                        try {
                                            //Lưu ảnh vào file
                                            FileOutputStream fos = new FileOutputStream(file);
                                            GeneralFunc.unzipBase64ToImg(task.getResult().getValue()
                                                    .toString()).compress(Bitmap.CompressFormat.JPEG,
                                                    100, fos);
                                            fos.close();

                                            //Thông báo để GALLERY cập nhật
                                            viewPic.this.sendBroadcast( makeMainSelectorActivity(
                                                    android.content.Intent.ACTION_MAIN,
                                                    android.content.Intent.CATEGORY_APP_GALLERY));

                                            Toast.makeText(viewPic.this,"Tải xuống thành công",
                                                    Toast.LENGTH_SHORT).show();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                            return true;
                        }
                    });

                    //Hiện menu setting
                    popupMenu.show();
                }
            });

            //Nút theo dõi
            binding.bViewPicFollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(binding.bViewPicFollow.getTag().toString().equals("0")){
                        mDB.child(pic.getB64EmailOwner()).child("follower")
                                .child(user.getB64Email()).setValue(true);
                        mDB.child(user.getB64Email()).child("follow")
                                .child(pic.getB64EmailOwner()).setValue(true);

                        binding.bViewPicFollow.setText("Bỏ theo dõi");
                        binding.bViewPicFollow.setBackgroundTintList(ContextCompat
                                .getColorStateList(viewPic.this,
                                        R.color.red));
                        binding.bViewPicFollow.setTag("1");
                    }else {
                        mDB.child(pic.getB64EmailOwner()).child("follower")
                                .child(user.getB64Email()).removeValue();
                        mDB.child(user.getB64Email()).child("follow")
                                .child(pic.getB64EmailOwner()).removeValue();

                        binding.bViewPicFollow.setText("Theo dõi");
                        binding.bViewPicFollow.setBackgroundTintList(ContextCompat
                                .getColorStateList(viewPic.this,
                                        R.color.gray));
                        binding.bViewPicFollow.setTag("0");
                    }

                    //Cập nhật lại số người theo dõi
                    mDB.child(pic.getB64EmailOwner()).child("follower").get().addOnCompleteListener(
                            new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    if(task.isSuccessful()){
                                        binding.tvViewPicNumberOfFollower.setText(String.valueOf(task.getResult()
                                                .getChildrenCount())+" người theo dõi");
                                    }
                                }
                            });
                }
            });

            //Nút love
            binding.ibViewPicLove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(binding.ibViewPicLove.getTag().toString().equals("0")){
                        mDB.child(pic.getB64EmailOwner()).child("pics").child(pic.getKey())
                                .child("lover").child(user.getB64Email()).setValue(true);
                        mDB.child(user.getB64Email()).child("love")
                                .child(pic.getB64EmailOwner()).child(pic.getKey()).setValue(true);

                        binding.ibViewPicLove.setImageDrawable(getDrawable(
                                R.drawable.baseline_favorite_24));
                        binding.ibViewPicLove.setTag("1");
                    }else {
                        mDB.child(pic.getB64EmailOwner()).child("pics").child(pic.getKey())
                                .child("lover").child(user.getB64Email()).removeValue();
                        mDB.child(user.getB64Email()).child("love")
                                .child(pic.getB64EmailOwner()).child(pic.getKey()).removeValue();

                        binding.ibViewPicLove.setImageDrawable(getDrawable(
                                R.drawable.baseline_favorite_border_24));
                        binding.ibViewPicLove.setTag("0");
                    }

                    //Cập nhật lại số love
                    mDB.child(pic.getB64EmailOwner()).child("pics").child(pic.getKey())
                            .child("lover").get().addOnCompleteListener(
                                    new OnCompleteListener<DataSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                                            if(task.isSuccessful()){
                                                binding.tvViewPicNumberOfLove.setText(String.valueOf(task.getResult()
                                                        .getChildrenCount()));
                                            }
                                        }
                                    });
                }
            });

            //Click ảnh đại diện/tên tk
            binding.ivViewPicProfilePic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent1=new Intent(viewPic.this, viewUser.class);
                    intent1.putExtra("user",user);
                    intent1.putExtra("targetUserB64Email",pic.getB64EmailOwner());

                    startActivity(intent1);
                }
            });
            binding.tvViewPicUsername.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent1=new Intent(viewPic.this, viewUser.class);
                    intent1.putExtra("user",user);
                    intent1.putExtra("targetUserB64Email",pic.getB64EmailOwner());

                    startActivity(intent1);
                }
            });
        }
    }
}