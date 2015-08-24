package ly.kite.instagramphotopicker;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.ListAdapter;

import java.util.List;

import in.srain.cube.views.GridViewWithHeaderAndFooter;


public class PagingGridView extends GridViewWithHeaderAndFooter {

	public interface Pagingable {
		void onLoadMoreItems();
	}

	private boolean isLoading;
	private boolean hasMoreItems;
	private Pagingable pagingableListener;
	private LoadingView loadinView;

	public PagingGridView(Context context) {
		super(context);
		init();
	}

	public PagingGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public PagingGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public boolean isLoading() {
		return this.isLoading;
	}

	public void setIsLoading(boolean isLoading) {
		this.isLoading = isLoading;
	}

	public void setPagingableListener(Pagingable pagingableListener) {
		this.pagingableListener = pagingableListener;
	}

	public void setHasMoreItems(boolean hasMoreItems) {
		this.hasMoreItems = hasMoreItems;
		if(!this.hasMoreItems) {
			removeFooterView(loadinView);
		}
	}

	public boolean hasMoreItems() {
		return this.hasMoreItems;
	}


	public void onFinishLoading(boolean hasMoreItems, List<? extends Object> newItems) {
		setHasMoreItems(hasMoreItems);
		setIsLoading(false);
		if(newItems != null && newItems.size() > 0) {
			ListAdapter adapter = super.getOriginalAdapter();
			if(adapter instanceof PagingBaseAdapter ) {
				((PagingBaseAdapter)adapter).addMoreItems(newItems);
			}
		}
	}

	private void init() {
		isLoading = false;
		loadinView = new LoadingView(getContext());
		addFooterView(loadinView);
		setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				//DO NOTHING...
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (totalItemCount > 0) {
					int lastVisibleItem = firstVisibleItem + visibleItemCount;
					if (!isLoading && hasMoreItems && (lastVisibleItem == totalItemCount)) {
						if (pagingableListener != null) {
							isLoading = true;
							pagingableListener.onLoadMoreItems();
						}

					}
				}
			}
		});
	}


}
