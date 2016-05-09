package com.github.c.horizonalscrollitem;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private List<String> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        MyAdapter myAdapter = new MyAdapter();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(myAdapter);

        list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(i + "");
        }
    }


    class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyVH> {


        @Override
        public MyVH onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
            View view = inflater.inflate(R.layout.item, parent, false);
            return new MyVH(view);
        }

        @Override
        public void onBindViewHolder(final MyVH holder, int position) {



        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class MyVH extends RecyclerView.ViewHolder {
            private ScrollerLayout mScrollerLayout;

            public MyVH(View itemView) {
                super(itemView);
                 mScrollerLayout = (ScrollerLayout) itemView.findViewById(R.id.scrollerLayout);
            }
        }
    }
}
