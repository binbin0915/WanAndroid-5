package com.zlx.module_public.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener;
import com.zlx.module_base.base_fg.BaseFg;
import com.zlx.module_base.constant.RouterConstant;
import com.zlx.module_network.api1.livedata.BaseObserver;
import com.zlx.module_network.api1.livedata.BaseObserverCallBack;
import com.zlx.module_network.bean.ApiResponse;
import com.zlx.module_network.res_data.ArticleListRes;
import com.zlx.module_network.res_data.PublicAuthorListRes;
import com.zlx.module_network.util.ApiUtil;
import com.zlx.module_public.R;
import com.zlx.module_public.R2;
import com.zlx.module_public.adapters.AuthorAdapter;
import com.zlx.module_public.guillotine.animation.GuillotineAnimation;
import com.zlx.widget.hivelayoutmanager.HiveLayoutManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Copyright (C)
 * FileName: PublicFg
 * Author: Zlx
 * Email: 1170762202@qq.com
 * Date: 2020/9/17 11:21
 * Description: 公众号
 */
@Route(path = RouterConstant.ROUT_FG_PUBLIC)
public class PublicFg extends BaseFg implements OnRefreshLoadMoreListener {
    private static final long RIPPLE_DURATION = 250;


    @BindView(R2.id.ivClose)
    ImageView ivClose;
    @BindView(R2.id.toolbar1)
    Toolbar toolbar;
    @BindView(R2.id.root)
    FrameLayout root;
    @BindView(R2.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R2.id.tvTitle)
    TextView tvTitle;
    @BindView(R2.id.smartRefreshLayout)
    SmartRefreshLayout smartRefreshLayout;

    RecyclerView rvAuthor;
    private List<PublicAuthorListRes> authList = new ArrayList<>();
    private AuthorAdapter authorAdapter;
    private GuillotineAnimation guillotineAnimation;

    //    private HomeArticleAdapter homeArticleAdapter;
    private List<ArticleListRes.DatasBean> articleList = new ArrayList<>();

    @Override
    protected int getLayoutId() {
        return R.layout.fg_public;
    }

    @Override
    protected boolean immersionBar() {
        return true;
    }


    @Override
    protected void initViews() {

        View guillotineMenu = LayoutInflater.from(getContext()).inflate(R.layout.guillotine, null);
        rvAuthor = guillotineMenu.findViewById(R.id.rvAuthor);
        HiveLayoutManager layoutManager = new HiveLayoutManager(HiveLayoutManager.VERTICAL);
        layoutManager.setGravity(HiveLayoutManager.ALIGN_LEFT);
//        layoutManager.setGravity(HiveLayoutManager.ALIGN_RIGHT);
//        layoutManager.setGravity(HiveLayoutManager.ALIGN_TOP);
//        layoutManager.setGravity(HiveLayoutManager.ALIGN_BOTTOM);
//        layoutManager.setGravity(HiveLayoutManager.ALIGN_LEFT | HiveLayoutManager.ALIGN_TOP);
//        layoutManager.setGravity(HiveLayoutManager.ALIGN_LEFT | HiveLayoutManager.ALIGN_BOTTOM);
//        layoutManager.setGravity(HiveLayoutManager.ALIGN_RIGHT | HiveLayoutManager.ALIGN_TOP);
//        layoutManager.setGravity(HiveLayoutManager.ALIGN_RIGHT | HiveLayoutManager.ALIGN_BOTTOM);
//        layoutManager.setPadding(0, 0, 0, 0);
        rvAuthor.setLayoutManager(layoutManager);
        authorAdapter = new AuthorAdapter(authList);
        authorAdapter.setListener(position -> {
            guillotineAnimation.close();
            PublicAuthorListRes publicAuthorListRes = authList.get(position);
            String name = publicAuthorListRes.getName();
            tvTitle.setText(name);
            id = publicAuthorListRes.getId();
            listArticle(id, true);
        });
        rvAuthor.setAdapter(authorAdapter);
        root.addView(guillotineMenu);
        guillotineAnimation = new GuillotineAnimation.GuillotineBuilder(guillotineMenu, guillotineMenu.findViewById(R.id.ivHamburger), ivClose)
                .setStartDelay(RIPPLE_DURATION)
                .setActionBarViewForAnimation(toolbar)
                .setClosedOnStart(true)
                .build();


        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//        homeArticleAdapter = new HomeArticleAdapter(articleList);
//        recyclerView.setAdapter(homeArticleAdapter);

        smartRefreshLayout.setEnableLoadMore(true);
        smartRefreshLayout.setEnableRefresh(true);
        smartRefreshLayout.setOnRefreshLoadMoreListener(this);
        listAuthor();
    }

    private int page = 0;
    private String id;

    private void listArticle(String id, boolean refresh) {
        if (!refresh) {
            page++;
        } else {
            page = 0;
        }
        ApiUtil.getArticleApi().listArticle(id, page).observe(this,new BaseObserver<>(new BaseObserverCallBack<ApiResponse<ArticleListRes>>() {
            @Override
            public void onSuccess(ApiResponse<ArticleListRes> data) {
                if (refresh) {
                    articleList.clear();
                }
                articleList.addAll(data.getData().getDatas());
//                homeArticleAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFinish() {
                super.onFinish();

                smartRefreshLayout.finishRefresh();
                smartRefreshLayout.finishLoadMore();
            }
        }));

    }

    private void listAuthor() {

        ApiUtil.getArticleApi().listPublicAuthor().observe(this,new BaseObserver<>(new BaseObserverCallBack<ApiResponse<List<PublicAuthorListRes>>>() {
            @Override
            public void onSuccess(ApiResponse<List<PublicAuthorListRes>> data) {
                authorAdapter.refresh(data.getData());
                if (data.getData().size() > 0) {
                    PublicAuthorListRes publicAuthorListRes = data.getData().get(0);
                    String name = publicAuthorListRes.getName();
                    tvTitle.setText(name);
                    id = publicAuthorListRes.getId();
                    listArticle(id, true);

                }
            }
        }));
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        listArticle(id, false);
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        listArticle(id, true);

    }
}

