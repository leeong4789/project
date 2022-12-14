package com.icia.friend;

import static com.icia.friend.remote.CartRemoteService.BASE_URL;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.icia.friend.session.Session;
import com.icia.friend.post.PostListActivity;
import com.icia.friend.post.PostInsertActivity;
import com.icia.friend.remote.CartRemoteService;
import com.icia.friend.remote.StoreRemoteService;
import com.icia.friend.store.StoreReadActivity;
import com.icia.friend.vo.CartVO;
import com.icia.friend.vo.StoreVO;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CartActivity extends AppCompatActivity {

    Retrofit retrofit, retrofit2;
    StoreRemoteService s_service;
    CartRemoteService c_service;

    RecyclerView list;
    TextView s_name, c_total;
    Button cart_addbutton, cart_post, create_button;
    CartAdapter CartAdapter = new CartAdapter();

    StoreVO vo = new StoreVO();
    List<CartVO> CartList = new ArrayList<>();
    String u_code;
    String s_code;
    int total = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        c_service = retrofit.create(CartRemoteService.class);

        retrofit2 = new Retrofit.Builder().baseUrl("http://192.168.0.193:8088/api/store/").addConverterFactory(GsonConverterFactory.create()).build();
        s_service = retrofit2.create(StoreRemoteService.class);

        s_name = findViewById(R.id.s_name);
        list = findViewById(R.id.cart_list);
        cart_addbutton = findViewById(R.id.cart_addbutton);
        c_total = findViewById(R.id.c_total);
        cart_post = findViewById(R.id.cart_post);
        create_button = findViewById(R.id.create_post);

        u_code = Session.getU_code(CartActivity.this);

        onRestart();

        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(CartAdapter);

        //????????? ??????
        cart_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CartList.size() == 0) {
                    Toast.makeText(CartActivity.this, "??????????????? ????????? ???????????????.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(CartActivity.this, PostListActivity.class);
                s_code = CartList.get(0).getS_code();
                intent.putExtra("s_code", s_code);
                total = 0;
                startActivity(intent);
            }
        });

        // ????????? ??????
        create_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CartList.size() == 0) {
                    Toast.makeText(CartActivity.this, "??????????????? ????????? ???????????????.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(CartActivity.this, PostInsertActivity.class);
                s_code = CartList.get(0).getS_code();
                intent.putExtra("s_code", s_code);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        total = 0;

        // ???????????? ??????
        Call<List<CartVO>> list = c_service.list(u_code);
        list.enqueue(new Callback<List<CartVO>>() {
            @Override
            public void onResponse(Call<List<CartVO>> call, Response<List<CartVO>> response) {
                CartList = response.body();
                if(CartList.size() != 0) {
                    s_code = CartList.get(0).getS_code();

                    Call<StoreVO> read = s_service.read(s_code);
                    read.enqueue(new Callback<StoreVO>() {
                        @Override
                        public void onResponse(Call<StoreVO> call, Response<StoreVO> response) {
                            vo = response.body();
                            s_name.setText(vo.getS_name());
                        }

                        @Override
                        public void onFailure(Call<StoreVO> call, Throwable t) {
                            System.out.println(".....?????? :: CartActivity - s_service.read : " + t.toString());
                        }
                    });
                }

                // ??????
                if (CartList.size() == 0) {
                    c_total.setText("0???");
                }
                CartAdapter.notifyDataSetChanged();

                // ??? ????????? ??????
                if (CartList.size() == 0) {
                    cart_addbutton.setVisibility(View.GONE);
                } else {
                    s_code = CartList.get(0).getS_code();
                    cart_addbutton.setVisibility(View.VISIBLE);
                }

                cart_addbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(CartActivity.this, StoreReadActivity.class);

                        Call<StoreVO> read = s_service.read(s_code);
                        read.enqueue(new Callback<StoreVO>() {
                            @Override
                            public void onResponse(Call<StoreVO> call, Response<StoreVO> response) {
                                StoreVO vo = response.body();
                                intent.putExtra("StoreVO", vo);
                                startActivity(intent);
                            }

                            @Override
                            public void onFailure(Call<StoreVO> call, Throwable t) {
                                System.out.println(".....?????? :: CartActivity - read : " + t.toString());
                            }
                        });
                    }
                });
            }

            @Override
            public void onFailure(Call<List<CartVO>> call, Throwable t) {
                System.out.println(".....?????? :: CartActivity - list : " + t.toString());
            }
        });
    }

    class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
        @NonNull
        @Override
        public CartAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_cart, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CartAdapter.ViewHolder holder, int position) {
            CartVO vo = CartList.get(position);
            total = total + (vo.getAmount() * vo.getM_price());
            c_total.setText(String.valueOf(total) + "???");
            holder.c_name.setText(vo.getM_name());
            holder.c_price.setText(String.valueOf(vo.getM_price()));
            holder.c_amount.setText(String.valueOf(vo.getAmount()));

            if (vo.getAmount() == 1) {
                holder.c_decrease.setVisibility(View.GONE);
            } else {
                holder.c_decrease.setVisibility(View.VISIBLE);
            }
            final Bitmap[] bitmap = new Bitmap[1];
            String imageUrl = "http://192.168.0.193:8088/api/display?fileName=" + vo.getM_photo();

            if (vo.getM_photo() != null) {
                holder.c_photo.setVisibility(View.VISIBLE);

                Thread Thread = new Thread() {

                    @Override
                    public void run() {
                        try {
                            URL url = new URL(imageUrl);
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                            // HttpURLConnection??? ??????????????? ??? ??? ???????????? ??????????????? ????????????
                            // conn.setDoInput(true);   //Server ???????????? ?????? ????????? ????????? ??????
                            conn.connect();
                            InputStream is = conn.getInputStream();     // inputStream ??? ????????????
                            bitmap[0] = BitmapFactory.decodeStream(is); // Bitmap?????? ??????
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
                Thread.start();

                try {
                    // join() ???????????? ????????? ?????? Thread??? ????????? ????????? ?????? Thread??? ?????????
                    Thread.join();
                    holder.c_photo.setImageBitmap(bitmap[0]);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                holder.c_photo.setVisibility(View.GONE);
            }

            // ?????? ??????
            holder.c_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Call<Void> delete = c_service.delete(vo);
                    delete.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            Toast.makeText(CartActivity.this, "?????????????????? ????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                            onRestart();
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            System.out.println(".....?????? :: CartActivity - delete : " + t.toString());
                        }
                    });
                }
            });

            // ?????? ??????
            holder.c_increase.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    holder.c_amount.setText(String.valueOf(vo.getAmount()+1));
                    vo.setAmount(vo.getAmount() + 1);
                    Call<Void> update = c_service.update(vo);
                    update.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            onRestart();
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            System.out.println(".....?????? :: CartActivity - update(??????) : " + t.toString());
                        }
                    });
                }
            });

            if (vo.getAmount() != 1) {
                holder.c_decrease.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        vo.setAmount(vo.getAmount() - 1);
                        Call<Void> update = c_service.update(vo);
                        update.enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                onRestart();
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                System.out.println(".....?????? :: CartActivity - update(??????) : " + t.toString());
                            }
                        });
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return CartList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            Button c_increase, c_decrease;
            TextView c_name, c_price, c_amount;
            ImageView c_photo, c_delete;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                c_name = itemView.findViewById(R.id.c_name);
                c_price = itemView.findViewById(R.id.c_price);
                c_amount = itemView.findViewById(R.id.c_amount);
                c_photo = itemView.findViewById(R.id.c_photo);
                c_delete = itemView.findViewById(R.id.c_delete);
                c_increase = itemView.findViewById(R.id.c_increase);
                c_decrease = itemView.findViewById(R.id.c_decrease);
            }
        }
    }

}