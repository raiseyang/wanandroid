package com.xiaojianjun.wanandroid.ui.main.discovery

import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.xiaojianjun.wanandroid.R
import com.xiaojianjun.wanandroid.common.ScrollToTop
import com.xiaojianjun.wanandroid.model.bean.Article
import com.xiaojianjun.wanandroid.model.bean.Banner
import com.xiaojianjun.wanandroid.ui.base.BaseVmFragment
import com.xiaojianjun.wanandroid.ui.detail.DetailActivity
import com.xiaojianjun.wanandroid.ui.detail.DetailActivity.Companion.PARAM_ARTICLE
import com.xiaojianjun.wanandroid.ui.main.MainActivity
import com.xiaojianjun.wanandroid.ui.search.SearchActivity
import com.xiaojianjun.wanandroid.ui.share.ShareActivity
import com.xiaojianjun.wanandroid.util.core.ActivityManager
import com.youth.banner.BannerConfig
import com.youth.banner.Transformer
import kotlinx.android.synthetic.main.fragment_discovery.*
import kotlinx.android.synthetic.main.include_reload.*

class DiscoveryFragment : BaseVmFragment<DiscoveryViewModel>(), ScrollToTop {

    private lateinit var hotWordsAdapter: HotWordsAdapter

    companion object {
        fun newInstance() = DiscoveryFragment()
    }

    override fun layoutRes() = R.layout.fragment_discovery

    override fun viewModelClass() = DiscoveryViewModel::class.java

    override fun initView() {
        ivAdd.setOnClickListener {
            checkLogin { ActivityManager.start(ShareActivity::class.java) }
        }
        ivSearch.setOnClickListener {
            ActivityManager.start(SearchActivity::class.java)
        }
        swipeRefreshLayout.run {
            setColorSchemeResources(R.color.textColorPrimary)
            setProgressBackgroundColorSchemeResource(R.color.bgColorPrimary)
            setOnRefreshListener { mViewModel.getData() }
        }

        hotWordsAdapter = HotWordsAdapter(R.layout.item_hot_word).apply {
            bindToRecyclerView(rvHotWord)
            setOnItemClickListener { _, _, _ -> }
        }
        btnReload.setOnClickListener {
            mViewModel.getData()
        }
        nestedScollView.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            if (activity is MainActivity && scrollY != oldScrollY) {
                (activity as MainActivity).animateBottomNavigationView(scrollY < oldScrollY)
            }
        }
    }

    override fun observe() {
        super.observe()
        mViewModel.run {
            banners.observe(viewLifecycleOwner, Observer {
                setupBanner(it)
            })
            hotWords.observe(viewLifecycleOwner, Observer {
                hotWordsAdapter.setNewData(it)
                tvHotWordTitle.isVisible = it.isNotEmpty()
            })
            frequentlyList.observe(viewLifecycleOwner, Observer {
                tagFlowLayout.adapter = TagAdapter(it)
                tagFlowLayout.setOnTagClickListener { _, position, _ ->
                    val frequently = it[position]
                    ActivityManager.start(
                        DetailActivity::class.java,
                        mapOf(
                            PARAM_ARTICLE to Article(
                                title = frequently.name,
                                link = frequently.link
                            )
                        )
                    )
                    false
                }
                tvFrquently.isGone = it.isEmpty()
            })
            refreshStatus.observe(viewLifecycleOwner, Observer {
                swipeRefreshLayout.isRefreshing = it
            })
            reloadStatus.observe(viewLifecycleOwner, Observer {
                reloadView.isVisible = it
            })
        }
    }

    private fun setupBanner(banners: List<Banner>) {
        bannerView.run {
            setBannerStyle(BannerConfig.NOT_INDICATOR)
            setImageLoader(BannerImageLoader())
            setImages(banners)
            setBannerAnimation(Transformer.BackgroundToForeground)
            start()
            setOnBannerListener {
                val banner = banners[it]
                ActivityManager.start(
                    DetailActivity::class.java,
                    mapOf(PARAM_ARTICLE to Article(title = banner.title, link = banner.url))
                )
            }
        }
    }

    override fun initData() {
        mViewModel.getData()
    }

    override fun scrollToTop() {
        nestedScollView?.smoothScrollTo(0, 0)
    }

    override fun onResume() {
        super.onResume()
        bannerView.startAutoPlay()
    }

    override fun onPause() {
        super.onPause()
        bannerView.stopAutoPlay()
    }
}
