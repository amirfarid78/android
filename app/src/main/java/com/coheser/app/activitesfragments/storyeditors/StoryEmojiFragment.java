package com.coheser.app.activitesfragments.storyeditors;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coheser.app.R;
import com.coheser.app.adapters.StoryEmojiAdapter;
import com.coheser.app.databinding.FragmentStoryEmojiBinding;
import com.coheser.app.interfaces.AdapterClickListener;
import com.coheser.app.interfaces.FragmentCallBack;
import com.coheser.app.simpleclasses.Functions;

import java.util.ArrayList;


public class StoryEmojiFragment extends Fragment {

    FragmentCallBack callBack;
    FragmentStoryEmojiBinding binding;
    StoryEmojiAdapter adapter;
    ArrayList<String> dataList = new ArrayList<>();

    public StoryEmojiFragment(FragmentCallBack callBack) {
        this.callBack = callBack;
    }

    public StoryEmojiFragment() {
    }

    public static StoryEmojiFragment newInstance(FragmentCallBack callBack) {
        StoryEmojiFragment fragment = new StoryEmojiFragment(callBack);
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_story_emoji, container, false);
        initControl();
        actionControl();
        return binding.getRoot();
    }

    private void actionControl() {

    }

    private void initControl() {

        setupAdapter();
    }

    private void setupAdapter() {
        GridLayoutManager layoutManager = new GridLayoutManager(binding.getRoot().getContext(), 5);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        binding.recylerview.setLayoutManager(layoutManager);
        adapter = new StoryEmojiAdapter(dataList, new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {
                String item = dataList.get(pos);
                Bundle bundle = new Bundle();
                bundle.putBoolean("isShow", true);
                bundle.putString("type", "emoji");
                bundle.putString("data", item);
                callBack.onResponce(bundle);
            }
        });
        binding.recylerview.setAdapter(adapter);
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (menuVisible) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    getEmojiList();
                }
            }, 200);
        }
    }

    private void getEmojiList() {
        if (!(dataList.size() > 0)) {
            String[] emojiArray = binding.getRoot().getContext().getResources().getStringArray(R.array.photo_editor_emoji);
            for (String emoji : emojiArray) {
                try {
                    dataList.add(Functions.convertEmoji(emoji));
                } catch (Exception e) {
                }
            }
            adapter.notifyDataSetChanged();
        }

        if (dataList.size() > 0) {
            binding.progressBar.setVisibility(View.GONE);
        }
    }


}