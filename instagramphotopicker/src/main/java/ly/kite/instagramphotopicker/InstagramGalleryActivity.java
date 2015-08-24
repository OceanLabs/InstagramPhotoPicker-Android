package ly.kite.instagramphotopicker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class InstagramGalleryActivity extends Activity {

    private static final String KEY_SELECTED_PHOTOS = "ly.kite.instagramphotopicker.KEY_SELECTED_PHOTOS";
    private static final String KEY_HAS_MORE_PHOTOS = "ly.kite.instagramphotopicker.KEY_HAS_MORE_PHOTOS";
    private static final String KEY_FETCHED_PHOTOS = "ly.kite.instagramphotopicker.KEY_FETCHED_PHOTOS";
    private static final String KEY_NEXT_PAGE_REQUEST = "ly.kite.instagramphotopicker.KEY_NEXT_PAGE_REQUEST";

    private static final int REQUEST_CODE_LOGIN = 99;
    private final HashSet<InstagramPhoto> selectedPhotos = new HashSet<>();

    static void startForResult(Activity activity, int requestCode) {
        Intent i = new Intent(activity, InstagramGalleryActivity.class);
        activity.startActivityForResult(i, requestCode);
    }

    static void startForResult(Fragment fragment, int requestCode) {
        Intent i = new Intent(fragment.getActivity(), InstagramGalleryActivity.class);
        fragment.startActivityForResult(i, requestCode);
    }

    private PagingGridView gridView;
    private InstagramPhotoAdapter adapter;
    private InstagramMediaRequest nextPageRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instagram_gallery);

        nextPageRequest = new InstagramMediaRequest();
        gridView = (PagingGridView) findViewById(R.id.gridview);

        List<InstagramPhoto> photos = new ArrayList<>();
        boolean hasMoreItems = true;
        if (savedInstanceState != null) {
            Parcelable[] fetchedPhotos = savedInstanceState.getParcelableArray(KEY_FETCHED_PHOTOS);
            for (Parcelable photo : fetchedPhotos) {
                photos.add((InstagramPhoto) photo);
            }

            Parcelable[] selected = savedInstanceState.getParcelableArray(KEY_SELECTED_PHOTOS);
            for (Parcelable photo : selected) {
                selectedPhotos.add((InstagramPhoto) photo);
            }

            hasMoreItems = savedInstanceState.getBoolean(KEY_HAS_MORE_PHOTOS);
            nextPageRequest = savedInstanceState.getParcelable(KEY_NEXT_PAGE_REQUEST);
        }

        adapter = new InstagramPhotoAdapter(photos);
        gridView.setAdapter(adapter);
        gridView.setHasMoreItems(hasMoreItems);
        gridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
        gridView.setMultiChoiceModeListener(new MultiChoiceModeListener());

        final String accessToken = InstagramPhotoPicker.getAccessToken(this);

        gridView.setPagingableListener(new PagingGridView.Pagingable() {
            @Override
            public void onLoadMoreItems() {
                nextPageRequest.getMedia(accessToken, new InstagramMediaRequest.InstagramMediaRequestListener() {
                    @Override
                    public void onMedia(List<InstagramPhoto> media, InstagramMediaRequest nextPageRequest) {
                        if(gridView.getAdapter() == null) {
                            gridView.setAdapter(new InstagramPhotoAdapter());
                        }

                        InstagramGalleryActivity.this.nextPageRequest = nextPageRequest;
                        gridView.onFinishLoading(nextPageRequest != null, media);
                    }

                    @Override
                    public void onError(Exception error) {
                        if (error instanceof InstagramPhotoPickerException) {
                            InstagramPhotoPickerException ex = (InstagramPhotoPickerException) error;
                            if (ex.getCode() == InstagramPhotoPickerException.CODE_INVALID_ACCESS_TOKEN) {
                                logout();
                            } else {
                                showErrorDialog(ex.getLocalizedMessage());
                            }
                        } else {
                            showErrorDialog(error.getLocalizedMessage());
                        }
                    }
                });
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                boolean checked = gridView.isItemChecked(position);
                gridView.setItemChecked(position, !checked);
            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArray(KEY_SELECTED_PHOTOS, selectedPhotos.toArray(new InstagramPhoto[0]));
        outState.putBoolean(KEY_HAS_MORE_PHOTOS, gridView.hasMoreItems());
        outState.putParcelableArray(KEY_FETCHED_PHOTOS, adapter.getItems().toArray(new InstagramPhoto[0]));
        outState.putParcelable(KEY_NEXT_PAGE_REQUEST, nextPageRequest);
    }

    private void showErrorDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage(message);
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private void logout() {
        String clientId = InstagramPhotoPicker.getClientId(this);
        String redirectUri = InstagramPhotoPicker.getRedirectUri(this);
        InstagramPhotoPicker.logout(this);
        InstagramPhotoPicker.startPhotoPickerForResult(this, clientId, redirectUri, REQUEST_CODE_LOGIN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // bubble result up to calling/starting activity
        setResult(resultCode, data);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_instagram_gallery, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            logout();
            return true;
        } else if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class InstagramPhotoAdapter extends PagingBaseAdapter<InstagramPhoto> {

        public InstagramPhotoAdapter(List<InstagramPhoto> items) {
            super(items);
        }

        public InstagramPhotoAdapter() {
            super();
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public InstagramPhoto getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            InstagramPhoto item = getItem(position);

            View view = null;

            if(convertView != null) {
                view = convertView;
            } else {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_cell, null);
                ViewHolder holder = new ViewHolder();
                holder.imageView = (ImageView) view.findViewById(R.id.imageview);
                holder.checkbox = (ImageView) view.findViewById(R.id.checkbox);
                view.setTag(holder);
            }

            ViewHolder holder = (ViewHolder) view.getTag();
            ImageView imageView = holder.imageView;
            Picasso.with(InstagramGalleryActivity.this).load(item.getThumbnailURL().toString()).into(imageView);

            holder.checkbox.setImageResource(gridView.isItemChecked(position) ? R.drawable.checkbox_on : R.drawable.checkbox_off);

            return view;
        }
    }

    private static final class ViewHolder {
        ImageView imageView;
        ImageView checkbox;
    }

    public class MultiChoiceModeListener implements
            GridView.MultiChoiceModeListener {



        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.photo_selection_menu, menu);

            int selectCount = gridView.getCheckedItemCount();
            mode.setTitle("" + selectCount);

            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.item_done) {
                Intent i = new Intent();
                InstagramPhoto[] photos = new InstagramPhoto[selectedPhotos.size()];
                selectedPhotos.toArray(photos);
                i.putExtra(InstagramPhotoPicker.EXTRA_SELECTED_PHOTOS, photos);
                setResult(Activity.RESULT_OK, i);
                finish();
            }

            return true;
        }

        public void onDestroyActionMode(ActionMode mode) {
        }

        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            int selectCount = gridView.getCheckedItemCount();
            mode.setTitle("" + selectCount);
            InstagramPhotoAdapter adapter = (InstagramPhotoAdapter) gridView.getOriginalAdapter();
            adapter.notifyDataSetChanged();

            InstagramPhoto photo = (InstagramPhoto) adapter.getItem(position);
            if (checked) {
                selectedPhotos.add(photo);
            } else {
                selectedPhotos.remove(photo);
            }
        }

    }
}

