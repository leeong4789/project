package com.icia.friend.post;

import static com.icia.friend.remote.PostRemoteService.BASE_URL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.icia.friend.R;
import com.icia.friend.remote.PostRemoteService;
import com.icia.friend.remote.UserRemoteService;
import com.icia.friend.session.Session;
import com.icia.friend.vo.PostVO;
import com.icia.friend.vo.UserVO;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PostListActivity extends AppCompatActivity {

    Retrofit retrofit;
    PostRemoteService p_service;
    UserRemoteService u_service;

    PostAdapter PostAdapter = new PostAdapter();

    List<PostVO> PostList = new ArrayList<>();
    String s_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_list);

        retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        p_service = retrofit.create(PostRemoteService.class);

        retrofit = new Retrofit.Builder().baseUrl("http://192.168.0.193:8088/api/user/").addConverterFactory(GsonConverterFactory.create()).build();
        u_service = retrofit.create(UserRemoteService.class);

        Intent intent = getIntent();
        s_code = intent.getStringExtra("s_code");
        System.out.println("PostListActivity - s_code : " + s_code);

        onRestart();

        RecyclerView p_list = findViewById(R.id.post_list);
        p_list.setLayoutManager(new LinearLayoutManager(this));
        p_list.setAdapter(PostAdapter);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Call<List<PostVO>> list = p_service.list(s_code);
        list.enqueue(new Callback<List<PostVO>>() {
            @Override
            public void onResponse(Call<List<PostVO>> call, Response<List<PostVO>> response) {
                PostList = response.body();
                PostAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<PostVO>> call, Throwable t) {
                System.out.println(".....?????? :: PostListActivity - list : " + t.toString());
            }
        });
    }

    class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
        @NonNull
        @Override
        public PostListActivity.PostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_post, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PostListActivity.PostAdapter.ViewHolder holder, int position) {
            String gender;
            PostVO vo = PostList.get(position);

            if (vo.getGender().equals("??????")) {
                gender = "??????";
            } else {
                gender = vo.getGender() + "???";
            }

            holder.p_title.setText(vo.getP_title());
            holder.p_count.setText(vo.getP_headcount() + " / " + vo.getHeadcount());
            holder.p_body.setText(vo.getP_content());
            holder.p_condition1.setText("?????? : " + vo.getAge() + "???");
            holder.p_condition2.setText("?????? : " + gender);

            // ?????? ?????? ??????
            UserVO userVO = new UserVO();
            Call<UserVO> call = u_service.read(Session.getU_code(PostListActivity.this));
            call.enqueue(new Callback<UserVO>() {
                @Override
                public void onResponse(Call<UserVO> call, Response<UserVO> response) {
                    userVO.setGender(response.body().getGender());
                    userVO.setAge(response.body().getAge());
                    System.out.println("PostListActivity - u_service.read : " + userVO.toString());
                }

                @Override
                public void onFailure(Call<UserVO> call, Throwable t) {
                    System.out.println(".....?????? :: PostListActivity - u_service.read : " + userVO.toString());
                }
            });

            // ?????? ?????? ???
            holder.btn_join.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // ?????? ?????? ?????? ?????? ?????? ???????????????
//                    System.out.println("(String.valueOf(vo.getAge()).substring(0, 0)) = " + (String.valueOf(vo.getAge()).substring(0, 1)));
                    // ?????? ?????? ?????? ??????
                    String conditionAge = (String.valueOf(vo.getAge()).substring(0, 1));
                    String userAge = (String.valueOf(userVO.getAge()).substring(0, 1));
                    
                    // ?????? ????????? ??????
                    if (!vo.getGender().equals("??????") && vo.getGender().equals(userVO.getGender()) && !conditionAge.equals(userAge) ||   // ????????? ????????? ????????? ????????? ????????? ????????? ????????? ?????? ???
                            !vo.getGender().equals("??????") && !vo.getGender().equals(userVO.getGender()) ||                               // ????????? ????????? ????????? ????????? ????????? ????????? ????????? ?????? ????????? ????????? ???????????? ?????? ???
                        !conditionAge.equals(userAge)) {                                                                                 // ????????? ????????? ???????????? ????????? ???????????? ?????? ?????? ???
                        Toast.makeText(PostListActivity.this, "????????? ??????????????????", Toast.LENGTH_SHORT).show();
                        return;
                    } else {    // ?????? ?????? ??????
                        Intent intent = new Intent(PostListActivity.this, PostReadActivity.class);
                        intent.putExtra("p_code", vo.getP_code());
                        startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return PostList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView p_title, p_count, p_body, p_condition1, p_condition2, p_condition3;
            Button btn_join;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                p_title = itemView.findViewById(R.id.p_title);
                p_count = itemView.findViewById(R.id.p_count);
                p_body = itemView.findViewById(R.id.p_body);
                p_condition1 = itemView.findViewById(R.id.p_condition1);
                p_condition2 = itemView.findViewById(R.id.p_condition2);
                btn_join = itemView.findViewById(R.id.btn_join);

            }
        }
    }

}