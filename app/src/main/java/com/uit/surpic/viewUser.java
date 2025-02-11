package com.uit.surpic;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.uit.surpic.databinding.ActivityViewUserBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class viewUser extends AppCompatActivity {
    ActivityViewUserBinding binding;
    objectUser user;
    String targetUserB64Email;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference mDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityViewUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ProgressBar progressBar = binding.pbViewUser;

        Intent intent=getIntent();
        if(intent!=null){
            progressBar.setVisibility(View.VISIBLE);

            //Kết nối FB Realtime DB
            firebaseDatabase= FirebaseDatabase.getInstance(
                    "https://surpic-324b6-default-rtdb.asia-southeast1.firebasedatabase.app");
            mDB=firebaseDatabase.getReference();

            //Nhận data
            user=intent.getParcelableExtra("user");
            targetUserB64Email=intent.getStringExtra("targetUserB64Email");

            ImageView userPic=binding.ivViewUserProfilePic;
            TextView tv_username=binding.tvViewUserUsername;

            //Tải ảnh đại diện
            mDB.child(targetUserB64Email).child(getString(R.string.profile_pic)).get()
                    .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            userPic.setImageBitmap(GeneralFunc.unzipBase64ToImg(String.valueOf(
                                    task.getResult().getValue())));
                        }
                    });

            //Lấy tên tài khoản và email
            mDB.child(targetUserB64Email).child("username").get()
                    .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            String username=String.valueOf(task.getResult().getValue());

                            binding.tvViewUser1.setText(binding.tvViewUser1.getText().toString()
                                    +username);
                            tv_username.setText(username);
                            binding.tvViewUserEmail.setText(GeneralFunc.base64ToStr(targetUserB64Email));
                        }
                    });

            //Cập nhật tình trạng theo dõi của người này
            mDB.child(targetUserB64Email).child("follower").get().addOnCompleteListener(
                    new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if(task.isSuccessful()){
                                binding.tvViewUserFollowerFollow.setText(String.valueOf(task.getResult()
                                        .getChildrenCount())+" người theo dõi");
                            }
                        }
                    });
            mDB.child(targetUserB64Email).child("follow").get().addOnCompleteListener(
                    new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if(task.isSuccessful()){
                                binding.tvViewUserFollowerFollow.setText(
                                        binding.tvViewUserFollowerFollow.getText().toString()+
                                                " & đang theo dõi "+ String.valueOf(task
                                                .getResult().getChildrenCount())+" người khác");
                            }
                        }
                    });

            //Kiểm tra đã theo dõi chưa
            mDB.child(user.getB64Email()).child("follow").child(targetUserB64Email).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if(task.isSuccessful()){
                        if(task.getResult().getValue()!=null){
                            binding.bViewUserFollow.setText("Bỏ theo dõi");
                            binding.bViewUserFollow.setBackgroundTintList(ContextCompat
                                    .getColorStateList(viewUser.this,
                                            R.color.red));
                            binding.bViewUserFollow.setTag("1");
                        } else {
                            binding.bViewUserFollow.setText("Theo dõi");
                            binding.bViewUserFollow.setBackgroundTintList(ContextCompat
                                    .getColorStateList(viewUser.this,
                                            R.color.orange_brown));
                            binding.bViewUserFollow.setTag("0");
                        }
                    }
                }
            });

            //Lấy danh sách ảnh của tài khoản
            mDB.child(targetUserB64Email).child("pics").get().addOnCompleteListener(
                    new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if(task.isSuccessful()){
                                ArrayList<LinearLayout> linearLayoutArrayList=new ArrayList<LinearLayout>();
                                for(DataSnapshot childSnapShot : task.getResult().getChildren()){
                                    LinearLayout linearLayout= GeneralFunc.itemPic(viewUser.this,new objectPic(
                                            childSnapShot.getKey(),
                                            String.valueOf(childSnapShot.child("data").getValue()),
                                            String.valueOf(childSnapShot.child("name").getValue()),
                                            String.valueOf(childSnapShot.child("tags").getValue()),
                                            targetUserB64Email));
                                    linearLayoutArrayList.add(linearLayout);
                                }

                                ConstraintLayout constraintLayout= binding.clViewUserProfileList;
                                constraintLayout.removeAllViews();
                                GeneralFunc.items2Layout(constraintLayout, linearLayoutArrayList,
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                ImageView selectedImg=v.findViewById(R.id.iv_itemPic),
                                                        vpImg = binding.ivViewUserVPImg;
                                                vpImg.setImageBitmap(((BitmapDrawable)selectedImg.getDrawable()).getBitmap());
                                                vpImg.setTag(selectedImg.getTag());
                                                vpImg.setTag(R.id.isZip,true);

                                                binding.ivViewUserVPProfilePic.setImageBitmap(
                                                        ((BitmapDrawable)userPic.getDrawable()).getBitmap());
                                                binding.tvViewUserVPUsername.setText(tv_username.getText().toString());

                                                objectPic pic=(objectPic)selectedImg.getTag();
                                                if(pic.getName().length()!=0 || pic.getName().equals("null")){
                                                    binding.tvViewUserVPNamePic.setText("Tên ảnh: "+pic.getName());
                                                }
                                                if(pic.getTags()[0].length() !=0 || pic.getTags()[0].equals("null")){
                                                    binding.tvViewUserVPTagsPic.setText("Tags: "+pic.getStrHashtags());
                                                }

                                                if(binding.bViewUserFollow.getTag().toString().equals("0")){
                                                    binding.bViewUserVPFollow.setText("Theo dõi");
                                                    binding.bViewUserVPFollow.setBackgroundTintList(ContextCompat
                                                            .getColorStateList(viewUser.this,
                                                                    R.color.gray));
                                                    binding.bViewUserVPFollow.setTag("0");
                                                }else {
                                                    binding.bViewUserVPFollow.setText("Bỏ theo dõi");
                                                    binding.bViewUserVPFollow.setBackgroundTintList(ContextCompat
                                                            .getColorStateList(viewUser.this,
                                                                    R.color.red));
                                                    binding.bViewUserVPFollow.setTag("1");
                                                }

                                                binding.clViewUserProfile.setVisibility(View.GONE);
                                                binding.clViewUserProfileVP.setVisibility(View.VISIBLE);
                                            }
                                        });
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    });

            //Cập nhật lại danh sách ảnh
            OnCompleteListener<DataSnapshot> onCompleteListener =
                    new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if(task.isSuccessful()){
                        ArrayList<LinearLayout> linearLayoutArrayList=new ArrayList<LinearLayout>();
                        for(DataSnapshot childSnapShot : task.getResult().getChildren()){
                            LinearLayout linearLayout= GeneralFunc.itemPic(viewUser.this,new objectPic(
                                    childSnapShot.getKey(),
                                    String.valueOf(childSnapShot.child("data").getValue()),
                                    String.valueOf(childSnapShot.child("name").getValue()),
                                    String.valueOf(childSnapShot.child("tags").getValue()),
                                    targetUserB64Email));
                            linearLayoutArrayList.add(linearLayout);
                        }

                        ConstraintLayout constraintLayout= binding.clViewUserProfileList;
                        constraintLayout.removeAllViews();
                        GeneralFunc.items2Layout(constraintLayout, linearLayoutArrayList,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ImageView selectedImg=v.findViewById(R.id.iv_itemPic),
                                                vpImg = binding.ivViewUserVPImg;
                                        vpImg.setImageBitmap(((BitmapDrawable)selectedImg.getDrawable()).getBitmap());
                                        vpImg.setTag(selectedImg.getTag());
                                        vpImg.setTag(R.id.isZip,true);

                                        binding.ivViewUserVPProfilePic.setImageBitmap(
                                                ((BitmapDrawable)userPic.getDrawable()).getBitmap());
                                        binding.tvViewUserVPUsername.setText(tv_username.getText().toString());

                                        objectPic pic=(objectPic)selectedImg.getTag();
                                        if(pic.getName().length()!=0 || pic.getName().equals("null")){
                                            binding.tvViewUserVPNamePic.setText("Tên ảnh: "+pic.getName());
                                        }
                                        if(pic.getTags()[0].length() !=0 || pic.getTags()[0].equals("null")){
                                            binding.tvViewUserVPTagsPic.setText("Tags: "+pic.getStrHashtags());
                                        }

                                        if(binding.bViewUserFollow.getTag().toString().equals("0")){
                                            binding.bViewUserVPFollow.setText("Theo dõi");
                                            binding.bViewUserVPFollow.setBackgroundTintList(ContextCompat
                                                    .getColorStateList(viewUser.this,
                                                            R.color.gray));
                                            binding.bViewUserVPFollow.setTag("0");
                                        }else {
                                            binding.bViewUserVPFollow.setText("Bỏ theo dõi");
                                            binding.bViewUserVPFollow.setBackgroundTintList(ContextCompat
                                                    .getColorStateList(viewUser.this,
                                                            R.color.red));
                                            binding.bViewUserVPFollow.setTag("1");
                                        }

                                        binding.clViewUserProfile.setVisibility(View.GONE);
                                        binding.clViewUserProfileVP.setVisibility(View.VISIBLE);
                                    }
                                });
                    }
                    progressBar.setVisibility(View.GONE);
                }
            };
            mDB.child(targetUserB64Email).child("pics").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    //Lấy danh sách ảnh của tài khoản
                    mDB.child(targetUserB64Email).child("pics").get()
                            .addOnCompleteListener(onCompleteListener);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    //Lấy danh sách ảnh của tài khoản
                    mDB.child(targetUserB64Email).child("pics").get()
                            .addOnCompleteListener(onCompleteListener);

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    //Lấy danh sách ảnh của tài khoản
                    mDB.child(targetUserB64Email).child("pics").get()
                            .addOnCompleteListener(onCompleteListener);

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            //Sự kiện click quay về của view pic
            binding.fabViewUserVPBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    binding.ivViewUserVPImg.setImageBitmap(null);
                    binding.ivViewUserVPProfilePic.setImageBitmap(null);
                    binding.tvViewUserVPUsername.setText("");
                    binding.tvViewUserVPNamePic.setText("");
                    binding.tvViewUserVPTagsPic.setText("");

                    binding.clViewUserProfile.setVisibility(View.VISIBLE);
                    binding.clViewUserProfileVP.setVisibility(View.GONE);
                }
            });

            //Nút more của view pic
            FloatingActionButton floatingActionButton = binding.fabViewUserVPMore;
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Khởi tạo menu setting
                    PopupMenu popupMenu=new PopupMenu(viewUser.this,floatingActionButton);

                    //Nhận item
                    popupMenu.getMenuInflater().inflate(R.menu.view_pic_more_menu,popupMenu.getMenu());

                    String string=binding.bViewUserFollow.getTag().toString();
                    boolean isFollowed=!binding.bViewUserFollow.getTag().toString().equals("0");
                    if(!isFollowed){
                        popupMenu.getMenu().getItem(1).setTitle("Theo dõi "+user.getUsername());
                    } else {
                        popupMenu.getMenu().getItem(1).setTitle("Bỏ theo dõi "+user.getUsername());
                    }
                    if((boolean) binding.ivViewUserVPImg.getTag(R.id.isZip)){
                        popupMenu.getMenu().getItem(0).setTitle("Xem ảnh bản đầy đủ");
                    }else {
                        popupMenu.getMenu().getItem(0).setTitle("Xem ảnh bản nén");
                    }

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            popupMenu.dismiss();

                            objectPic pic=(objectPic) binding.ivViewUserVPImg.getTag();

                            //Lựa chọn (bỏ) theo dõi
                            if(item.getItemId()==R.id.vp_more_i_follow){
                                if(!isFollowed){
                                    mDB.child(targetUserB64Email).child("follower")
                                            .child(user.getB64Email()).setValue(true);
                                    mDB.child(user.getB64Email()).child("follow")
                                            .child(targetUserB64Email).setValue(true);

                                    binding.bViewUserFollow.setText("Bỏ theo dõi");
                                    binding.bViewUserFollow.setBackgroundTintList(ContextCompat
                                            .getColorStateList(viewUser.this, R.color.red));
                                    binding.bViewUserVPFollow.setText("Bỏ theo dõi");
                                    binding.bViewUserVPFollow.setBackgroundTintList(ContextCompat
                                            .getColorStateList(viewUser.this, R.color.red));

                                    binding.bViewUserFollow.setTag("1");
                                    binding.bViewUserVPFollow.setTag("1");
                                }else {
                                    mDB.child(targetUserB64Email).child("follower")
                                            .child(user.getB64Email()).removeValue();
                                    mDB.child(user.getB64Email()).child("follow")
                                            .child(targetUserB64Email).removeValue();

                                    binding.bViewUserFollow.setText("Theo dõi");
                                    binding.bViewUserFollow.setBackgroundTintList(ContextCompat
                                            .getColorStateList(viewUser.this, R.color.orange_brown));
                                    binding.bViewUserVPFollow.setText("Theo dõi");
                                    binding.bViewUserVPFollow.setBackgroundTintList(ContextCompat
                                            .getColorStateList(viewUser.this, R.color.gray));

                                    binding.bViewUserFollow.setTag("0");
                                    binding.bViewUserVPFollow.setTag("0");
                                }

                                return true;
                            }

                            //Lựa chọn tải về
                            if(item.getItemId()==R.id.vp_more_i_download){
                                GeneralFunc.downloadImg(viewUser.this, mDB,
                                        targetUserB64Email, pic.getKey());

                                Toast.makeText(viewUser.this,"Đang tải...",Toast.LENGTH_SHORT).show();

                                return true;
                            }

                            //Lựa chọn xem bản đủ
                            if(item.getItemId()==R.id.vp_more_i_full_zip){
                                if((boolean)binding.ivViewUserVPImg.getTag(R.id.isZip)){
                                    binding.ivViewUserVPImg.setTag(R.id.isZip,false);

                                    new Handler().post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mDB.child(targetUserB64Email).child("pics")
                                                    .child(pic.getKey()).child("full")
                                                    .get().addOnCompleteListener(
                                                            new OnCompleteListener<DataSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                                    binding.ivViewUserVPImg.setImageBitmap(
                                                                            GeneralFunc.unzipBase64ToImg(
                                                                                    String.valueOf(
                                                                                            task.getResult()
                                                                                                    .getValue())));

                                                                    binding.pbViewUser
                                                                            .setVisibility(View.GONE);
                                                                    binding.ivViewUserVPImg.setVisibility(View.VISIBLE);
                                                                }
                                                            });
                                        }
                                    });

                                    binding.pbViewUser.setVisibility(View.VISIBLE);
                                    binding.ivViewUserVPImg.setVisibility(View.GONE);
                                } else {
                                    binding.ivViewUserVPImg.setTag(R.id.isZip,true);

                                    binding.ivViewUserVPImg.setImageBitmap(
                                            GeneralFunc.unzipBase64ToImg(pic.getData()));
                                }

                                return true;
                            }

                            return false;
                        }
                    });

                    //Hiện menu setting
                    popupMenu.show();
                }
            });

            //Nút theo dõi
            binding.bViewUserFollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(binding.bViewUserFollow.getTag().toString().equals("0")){
                        mDB.child(targetUserB64Email).child("follower")
                                .child(user.getB64Email()).setValue(true);
                        mDB.child(user.getB64Email()).child("follow")
                                .child(targetUserB64Email).setValue(true);

                        binding.bViewUserFollow.setText("Bỏ theo dõi");
                        binding.bViewUserFollow.setBackgroundTintList(ContextCompat
                                .getColorStateList(viewUser.this,
                                        R.color.red));
                        binding.bViewUserFollow.setTag("1");
                    }else {
                        mDB.child(targetUserB64Email).child("follower")
                                .child(user.getB64Email()).removeValue();
                        mDB.child(user.getB64Email()).child("follow")
                                .child(targetUserB64Email).removeValue();

                        binding.bViewUserFollow.setText("Theo dõi");
                        binding.bViewUserFollow.setBackgroundTintList(ContextCompat
                                .getColorStateList(viewUser.this,
                                        R.color.orange_brown));
                        binding.bViewUserFollow.setTag("0");
                    }
                }
            });

            //Nút theo dõi của view pic
            binding.bViewUserFollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(binding.bViewUserVPFollow.getTag().toString().equals("0")){
                        mDB.child(targetUserB64Email).child("follower")
                                .child(user.getB64Email()).setValue(true);
                        mDB.child(user.getB64Email()).child("follow")
                                .child(targetUserB64Email).setValue(true);

                        binding.bViewUserVPFollow.setText("Bỏ theo dõi");
                        binding.bViewUserVPFollow.setBackgroundTintList(ContextCompat
                                .getColorStateList(viewUser.this,
                                        R.color.red));
                        binding.bViewUserVPFollow.setTag("1");
                    }else {
                        mDB.child(targetUserB64Email).child("follower")
                                .child(user.getB64Email()).removeValue();
                        mDB.child(user.getB64Email()).child("follow")
                                .child(targetUserB64Email).removeValue();

                        binding.bViewUserVPFollow.setText("Theo dõi");
                        binding.bViewUserVPFollow.setBackgroundTintList(ContextCompat
                                .getColorStateList(viewUser.this,
                                        R.color.gray));
                        binding.bViewUserVPFollow.setTag("0");
                    }
                }
            });

            //Nút love
            binding.ibViewUserVPLove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    objectPic pic=(objectPic) binding.ivViewUserVPImg.getTag();
                    if(binding.ibViewUserVPLove.getTag().toString().equals("0")){
                        mDB.child(targetUserB64Email).child("pics").child(pic.getKey())
                                .child("lover").child(user.getB64Email()).setValue(true);
                        mDB.child(user.getB64Email()).child("love")
                                .child(targetUserB64Email).child(pic.getKey()).setValue(true);

                        binding.ibViewUserVPLove.setImageDrawable(getDrawable(
                                R.drawable.baseline_favorite_24));
                        binding.ibViewUserVPLove.setTag("1");
                    }else {
                        mDB.child(targetUserB64Email).child("pics").child(pic.getKey())
                                .child("lover").child(user.getB64Email()).removeValue();
                        mDB.child(user.getB64Email()).child("love")
                                .child(targetUserB64Email).child(pic.getKey()).removeValue();

                        binding.ibViewUserVPLove.setImageDrawable(getDrawable(
                                R.drawable.baseline_favorite_border_24));
                        binding.ibViewUserVPLove.setTag("0");
                    }
                }
            });

            //Nút quay về
            binding.fabViewUserBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

    }

}