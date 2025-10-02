package com.coheser.app.activitesfragments.storyeditors;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import com.coheser.app.R;
import com.coheser.app.adapters.ViewPagerAdapter;
import com.coheser.app.databinding.FragmentStoryStickerArtBinding;
import com.coheser.app.interfaces.FragmentCallBack;
import com.coheser.app.simpleclasses.Functions;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;


public class StoryStickerArtFragment extends BottomSheetDialogFragment {

    FragmentCallBack callBack;
    FragmentStoryStickerArtBinding binding;
    ViewPagerAdapter adapter;


    public StoryStickerArtFragment(FragmentCallBack callBack) {
        this.callBack = callBack;
    }

    public StoryStickerArtFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_story_sticker_art, container, false);
        initControl();
        actionControl();
        return binding.getRoot();
    }

    private void actionControl() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (binding.etSearch.getText().toString().length() > 0) {
                    binding.tvSearch.setVisibility(View.VISIBLE);

                } else {
                    binding.tvSearch.setVisibility(View.GONE);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.tvSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGiphySearch();
            }
        });
    }

    private void initControl() {
        SetTabs();
    }


    public void SetTabs() {

        adapter = new ViewPagerAdapter(this);

        binding.viewpager.setOffscreenPageLimit(3);
        registerFragmentWithPager();
        binding.viewpager.setAdapter(adapter);
        addTabs();

        binding.viewpager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                binding.tabs.getTabAt(position).select();
            }
        });


    }

    private void openGiphySearch() {
        Functions.hideSoftKeyboard(getActivity());
        StoryGiphyFragment fragment = new StoryGiphyFragment(new FragmentCallBack() {
            @Override
            public void onResponce(Bundle bundle) {
                if (bundle.getBoolean("isShow")) {
                    callBack.onResponce(bundle);
                    dismiss();
                }
            }
        });
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putString("searchKey", binding.etSearch.getText().toString());
        fragment.setArguments(bundle);
        transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
        transaction.addToBackStack("StoryGiphyF");
        transaction.replace(binding.tabGiphyContainer.getId(), fragment, "StoryGiphyF").commit();
    }

    private void registerFragmentWithPager() {
        adapter.addFrag(StoryStickersFragment.newInstance(new FragmentCallBack() {
            @Override
            public void onResponce(Bundle bundle) {
                if (bundle.getBoolean("isShow")) {
                    callBack.onResponce(bundle);
                    dismiss();
                }
            }
        }));
        adapter.addFrag(StoryEmojiFragment.newInstance(new FragmentCallBack() {
            @Override
            public void onResponce(Bundle bundle) {
                if (bundle.getBoolean("isShow")) {
                    callBack.onResponce(bundle);
                    dismiss();
                }
            }
        }));
    }

    private void addTabs() {
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(binding.tabs, binding.viewpager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                if (position == 0) {
                    tab.setText(binding.getRoot().getContext().getString(R.string.sticker));
                } else if (position == 1) {
                    tab.setText(binding.getRoot().getContext().getString(R.string.emoji));
                }
            }
        });
        tabLayoutMediator.attach();
    }


}