
package org.aisen.weibo.sina.ui.activity.main;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.bean.ApkInfo;
import org.aisen.weibo.sina.support.bean.MenuBean;
import org.aisen.weibo.sina.support.iclass.IAcNavigation;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.AppSettings;
import org.aisen.weibo.sina.support.utils.CheckChangedUtils;
import org.aisen.weibo.sina.ui.activity.common.WeiboBaseActivity;
import org.aisen.weibo.sina.ui.activity.publish.PublishActivity;
import org.aisen.weibo.sina.ui.activity.search.SearchActivity;
import org.aisen.weibo.sina.ui.fragment.comment.CommentPagerFragment;
import org.aisen.weibo.sina.ui.fragment.draft.DraftFragment;
import org.aisen.weibo.sina.ui.fragment.friendship.FriendshipPagerFragment;
import org.aisen.weibo.sina.ui.fragment.mention.Mention_v2Fragment;
import org.aisen.weibo.sina.ui.fragment.menu.MenuFragment_v2;
import org.aisen.weibo.sina.ui.fragment.menu.MenuFragment_v2.MenuCallback;
import org.aisen.weibo.sina.ui.fragment.profile.UserProfileFragment;
import org.aisen.weibo.sina.ui.fragment.settings.AboutWebFragment;
import org.aisen.weibo.sina.ui.fragment.settings.SettingsFragment;
import org.aisen.weibo.sina.ui.fragment.settings.VersionSettingsFragment;
import org.aisen.weibo.sina.ui.fragment.timeline.FavoritesFragment;
import org.aisen.weibo.sina.ui.fragment.timeline.MainTimelinePagerFragment;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.ikimuhendis.ldrawer.ActionBarDrawerToggle;
import com.ikimuhendis.ldrawer.DrawerArrowDrawable;

import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;

import com.alibaba.fastjson.JSON;
import com.m.common.context.GlobalContext;
import com.m.common.utils.ActivityHelper;
import com.m.common.utils.SystemUtility;
import com.m.support.Inject.ViewInject;
import com.m.ui.fragment.ABaseFragment;
import com.m.ui.fragment.CacheClearFragment;
import com.m.ui.utils.MToast;

public class MainActivity extends WeiboBaseActivity implements MenuCallback {

	public static void login() {
		Intent intent = new Intent(GlobalContext.getInstance(), MainActivity.class);
		intent.setAction(ACTION_LOGIN);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		GlobalContext.getInstance().startActivity(intent);
	}
	
	private static final String ACTION_LOGIN = "org.aisen.sina.weibo.LOGIN";
	
	@ViewInject(id = R.id.drawer)
	private DrawerLayout mDrawerLayout;
	
	private ActionBarDrawerToggle mDrawerToggle;
	private MenuBean lastSelectedMenu;
	
	private MenuFragment_v2 menuFragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		final org.aisen.weibo.sina.ui.fragment.base.ActivityHelper activityHelper = (org.aisen.weibo.sina.ui.fragment.base.ActivityHelper) getActivityHelper();
		activityHelper.blur = AppSettings.isMainBlur();
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_main);
		
		// 初始化信息弹窗
		MToast.yOffset = SystemUtility.getStatusBarHeight(this) * 2 + SystemUtility.getActionBarHeight(this);
		DrawerArrowDrawable drawerArrow = new DrawerArrowDrawable(this) {
            @Override
            public boolean isLayoutRtl() {
                return false;
            }
        };
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, drawerArrow,
				R.string.draw_open, R.string.draw_close) {
			
			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
				
				if (lastSelectedMenu != null) {
                    getActionBar().setTitle(lastSelectedMenu.getTitleRes());
                    invalidateOptionsMenu();
                }
				
				Fragment fragment = (Fragment) getFragmentManager().findFragmentByTag("MainFragment");
				if (fragment instanceof IAcNavigation) {
					getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
				}
			}
			
			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				
				getActionBar().setTitle(R.string.app_name);
				invalidateOptionsMenu();
				
				getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			}
			
			@Override
			public void onDrawerSlide(View drawerView, float slideOffset) {
				super.onDrawerSlide(drawerView, slideOffset);
			}
			
			@Override
			public void onDrawerStateChanged(int newState) {
				super.onDrawerStateChanged(newState);
			}
			
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		//getActionBar().setIcon(R.drawable.ic_ab_app);
		//getActionBar().setDisplayShowHomeEnabled(true);
		
		lastSelectedMenu = savedInstanceState == null ? null : (MenuBean) savedInstanceState.getSerializable("menu");
		
		if (ActivityHelper.getInstance().getBooleanShareData("isFirstLaunch", true)) {
			ActivityHelper.getInstance().putBooleanShareData("isFirstLaunch", false);
			
			mDrawerLayout.openDrawer(Gravity.LEFT);
		} else {
			if (lastSelectedMenu != null)
				getActionBar().setTitle(lastSelectedMenu.getTitleRes());
			else
				getActionBar().setTitle(R.string.draw_timeline);
		}
		
		if (savedInstanceState == null) {
			String action = getIntent() != null ? getIntent().getAction() : null;
			String type = null;
			
			// 处理点击Notification时，设置显示菜单
			if (ACTION_LOGIN.equals(action)) {
				type = "1";
			}
			else if ("showFollowers".equals(action)) {
				type = "4";
			}
			else if ("showComments".equals(action)) {
				type = "3";
			}
			else if ("showMentionStatus".equals(action)) {
				ActivityHelper.getInstance().putShareData("showMensitonType", "showMentionStatus");

				type = "2";
			}
			else if ("showMentionCmt".equals(action)) {
				ActivityHelper.getInstance().putShareData("showMensitonType", "showMentionCmt");

				type = "2";
			}
			else if ("showDraft".equals(action)) {
				type = "6";
			}
			
			menuFragment = MenuFragment_v2.newInstance(type);
			menuFragment.setMenuCallback(this);
			getFragmentManager().beginTransaction().add(R.id.menu_frame, menuFragment, "MenuFragment").commit();
			
		} else {
			menuFragment = (MenuFragment_v2) getFragmentManager().findFragmentByTag("MenuFragment");
			menuFragment.setMenuCallback(this);
			
			// 2014-8-30 解决因为状态保存而导致的耗时阻塞
			if (lastSelectedMenu.getType().equals("1"))
				onMenuSelected(lastSelectedMenu, true);
		}
		
		if (ActivityHelper.getInstance().getBooleanShareData("newVersion", false)) {
			String apkInfoJson = ActivityHelper.getInstance().getShareData("apkInfo", null);
			if (!TextUtils.isEmpty(apkInfoJson)) {
				ApkInfo apkInfo = JSON.parseObject(apkInfoJson, ApkInfo.class);
				if (!ActivityHelper.getInstance().getBooleanShareData("IgnoreNewVersion-" + apkInfo.getVersionCode(), false)) {
					VersionSettingsFragment.showNewVersionDialog(this, apkInfo, true);
				}
			}
		}
		
		// 每次启动应用，检测是否下载了壁纸
		CheckChangedUtils.checkWallpaper();
		
		// 2014-10-29 解决首次进入首页时，ActionBar的颜色总差一点点的BUG
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				activityHelper.wallpaper.setWallpaper();
			}
			
		}, 200);
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		if (intent != null) {
			String action = intent.getAction();
			
			lastSelectedMenu = null;
			
			MenuBean menuBean = null;
			if (ACTION_LOGIN.equals(action)) {
				menuBean = getMenu(1);
			}
			else if ("showFollowers".equals(action)) {
				menuBean = getMenu(4);
			}
			else if ("showComments".equals(action)) {
				menuBean = getMenu(3);
			}
			else if ("showMentionStatus".equals(action)) {
				ActivityHelper.getInstance().putShareData("showMensitonType", "showMentionStatus");
				
				menuBean = getMenu(2);
			}
			else if ("showMentionCmt".equals(action)) {
				ActivityHelper.getInstance().putShareData("showMensitonType", "showMentionCmt");
				
				menuBean = getMenu(2);
			}
			else if ("showDraft".equals(action)) {
				menuBean = getMenu(6);
			}
			
			if (menuBean != null) {
				onMenuSelected(menuBean, true);
				
				MenuFragment_v2 menuFragment = (MenuFragment_v2) getFragmentManager().findFragmentByTag("MenuFragment");
				menuFragment.setItemTextColor(menuBean);
				if ("1".equals(menuBean.getType()))
					menuFragment.setAccountItem();
			}
			
			if (isDrawerOpened())
				closeDrawer();
		}
	}
	
	private MenuBean getMenu(int type) {
		return MenuFragment_v2.newMenu(String.valueOf(type));
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		if (lastSelectedMenu != null)
			outState.putSerializable("menu", lastSelectedMenu);
	}
	
	@Override
	public boolean onMenuSelected(MenuBean menu, boolean replace) {
		if (!replace && lastSelectedMenu != null && lastSelectedMenu.getType().equals(menu.getType())) {
			closeDrawer();
			return true;
		}
		
		int type = Integer.parseInt(menu.getType());
		
		ActionBar actionBar = getActionBar();
		actionBar.setSubtitle(null);
		actionBar.setTitle(menu.getTitleRes());
		
		ABaseFragment fragment = null;
		
		switch (type) {
		// 授权用户信息
		case 0:
			fragment = UserProfileFragment.newInstance();
			break;
		// 微博首页
		case 1:
			fragment = MainTimelinePagerFragment.newInstance();
			break;
		// 提及
		case 2:
			// 显示提及的微博、评论的Navigation导航
//			fragment = MentionFragment.newInstance();
			// 修改为只有提及全部微博、全部评论，没有Navigation导航
			fragment = Mention_v2Fragment.newInstance();
			break;
		// 评论
		case 3:
			fragment = CommentPagerFragment.newInstance();
			break;
		// 朋友关系
		case 4:
			fragment = FriendshipPagerFragment.newInstance();
			break;
		// 草稿箱
		case 6:
			fragment = DraftFragment.newInstance();
			break;
        // 收藏的微博
        case 7:
            fragment = FavoritesFragment.newInstance();
            break;
		// 设置
		case 5:
			closeDrawer();
			
			SettingsFragment.launch(this);
			return true;
		// 搜索
		case 8:
			closeDrawer();
			
			SearchActivity.launch(this);
			return true;
		default:
			return true;
		}
		
		if (fragment instanceof IAcNavigation) {
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
			
			IAcNavigation onNavigation = (IAcNavigation) fragment;
			ArrayAdapter<CharSequence> spinnerAdapter = 
											ArrayAdapter.createFromResource(this, onNavigation.adapterResource(), R.layout.sherlock_spinner_item);
			spinnerAdapter.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
			actionBar.setListNavigationCallbacks(spinnerAdapter, onNavigation);
			actionBar.setSelectedNavigationItem(onNavigation.current());
		}
		else {
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		}
		
		getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment, "MainFragment").commit();
		
		lastSelectedMenu = menu;
		
		return false;
	}
	
	public void closeDrawer() {
		mDrawerLayout.closeDrawers();
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		
		mDrawerToggle.syncState();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.publish).setVisible(true);
		menu.findItem(R.id.help).setVisible(false);
		menu.findItem(R.id.about).setVisible(true);
		
		if (lastSelectedMenu != null && "1".equals(lastSelectedMenu.getType())) {
			
		}
		// 微博首页时，一直显示发布新微博
		if (lastSelectedMenu != null && "1".equals(lastSelectedMenu.getType()) && !isDrawerOpened()) {
			menu.findItem(R.id.publish).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}
		// 其他情况，放置到溢出菜单
		else {
			menu.findItem(R.id.publish).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER);
		}
		
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		resetAcUnused();
		
		if (mDrawerToggle.onOptionsItemSelected(item))
			return true;
		
		if (item.getItemId() == R.id.publish)
			PublishActivity.publishStatus(this, null);
		else if (item.getItemId() == R.id.help)
			AboutWebFragment.launchHelp(this);
		else if (item.getItemId() == R.id.about)
			AboutWebFragment.launchAbout(this);
		else if (item.getItemId() == R.id.feedback)
			PublishActivity.publishFeedback(this);
		
		return super.onOptionsItemSelected(item);
	}
	
	public boolean isDrawerOpened() {
		return mDrawerLayout.isDrawerOpen(Gravity.LEFT);
	}
	
	private boolean canFinish = false;
	
	@Override
	public boolean onBackClick() {
		if (!canFinish) {
			canFinish = true;

			showMessage(R.string.hint_exit);
			
			new Handler().postDelayed(new Runnable() {
				
				@Override
				public void run() {
					canFinish = false;
				}
				
			}, 1500);
			
			return true;
		}
		
		setMDestory(true);
		return super.onBackClick();
	}
	
	@Override
	protected void onResume() {
		final org.aisen.weibo.sina.ui.fragment.base.ActivityHelper activityHelper = (org.aisen.weibo.sina.ui.fragment.base.ActivityHelper) getActivityHelper();
		activityHelper.wallpaper.blur = AppSettings.isMainBlur();
		
		super.onResume();
		
		if (!AppContext.isLogedin())
			finish();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		// 清理压缩缓存
		CacheClearFragment.clearCompress();
	}
	
}
