package com.apprise.toggl;

import java.util.Calendar;

import com.apprise.toggl.remote.SyncService;
import com.apprise.toggl.storage.DatabaseAdapter;
import com.apprise.toggl.storage.DatabaseAdapter.Tasks;
import com.apprise.toggl.storage.models.User;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class TasksActivity extends ListActivity {

  private static final String TAG = "TasksActivity"; 
  
  private DatabaseAdapter dbAdapter;
  private SyncService syncService;
  private Toggl app;
  private User currentUser;
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    setProgressBarIndeterminate(true);
    setContentView(R.layout.tasks);
    
    init();
  }
  
  @Override
  protected void onResume() {
    IntentFilter filter = new IntentFilter(SyncService.SYNC_COMPLETED);
    registerReceiver(updateReceiver, filter);
    super.onResume();
  }
  
  @Override
  protected void onPause() {
    unregisterReceiver(updateReceiver);
    super.onPause();
  }
  
  @Override
  protected void onDestroy() {
    unbindService(syncConnection);
    super.onDestroy();
  }

  protected void init() {
    dbAdapter = new DatabaseAdapter(this);
    app = (Toggl) getApplication();
    currentUser = app.getCurrentUser();
    Intent intent = new Intent(this, SyncService.class);
    bindService(intent, syncConnection, BIND_AUTO_CREATE);
    populateList();
  }
  
  public void populateList() {
    SimpleCursorAdapter cursorAdapter;
    Cursor tasksCursor;    
    int taskRetentionDays = currentUser.task_retention_days;
    Calendar queryCal = (Calendar) Calendar.getInstance().clone();
    String[] fieldsToShow = { Tasks.DURATION, Tasks.DESCRIPTION };
    int[] viewsToFill = { R.id.task_item_duration, R.id.task_item_description };
    String date;
    String header_text;
    long duration_total = 0;
    
    dbAdapter.open();

    for (int i = 0; i < taskRetentionDays; i++) {
      queryCal.add(Calendar.DATE, -1);
      tasksCursor = dbAdapter.findTasksByDate(queryCal.getTime());
      cursorAdapter = new SimpleCursorAdapter(this, R.layout.task_item,
          tasksCursor, fieldsToShow, viewsToFill);
          date = Util.smallDateString(queryCal.getTime());
          
      while (tasksCursor.moveToNext()) {
        duration_total += tasksCursor.getLong(tasksCursor.getColumnIndex(Tasks.DURATION));
      }
      
      header_text = date + " (" + Util.secondsToHM(duration_total) + " h)";
      adapter.addSection(header_text, cursorAdapter);
    }
    
    setListAdapter(adapter);
    dbAdapter.close();
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.tasks_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }   

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.tasks_menu_refresh:
        setProgressBarIndeterminateVisibility(true);
        new Thread(refreshTasksInBackgrond).start();
        return true;
      case R.id.tasks_menu_account:
        startActivity(new Intent(this, AccountActivity.class));
        return true;
    }
    return super.onOptionsItemSelected(item);
  }  
  
  protected ServiceConnection syncConnection = new ServiceConnection() {
    
    public void onServiceDisconnected(ComponentName name) {}
    
    public void onServiceConnected(ComponentName name, IBinder serviceBinding) {
      SyncService.SyncBinder binding = (SyncService.SyncBinder) serviceBinding;
      syncService = binding.getService();
    }

  };
  
  protected Runnable refreshTasksInBackgrond = new Runnable() {
    
    public void run() {
      syncService.syncTasks();
    }
  };
  
  protected BroadcastReceiver updateReceiver = new BroadcastReceiver() {
    
    @Override
    public void onReceive(Context context, Intent intent) {
      adapter.clearSections();
      populateList();
      setProgressBarIndeterminateVisibility(false);      
    }
  };

  SectionedAdapter adapter = new SectionedAdapter() {
    protected View getHeaderView(String caption, int index, View convertView, ViewGroup parent) {
      LinearLayout result = (LinearLayout) convertView;

      if (convertView == null) {
        result = (LinearLayout) getLayoutInflater().inflate(
            R.layout.tasks_group_header, null);

      }

      TextView header = (TextView) result.findViewById(R.id.task_list_header_text);      
      header.setText(caption);

      return (result);
    }
  };

}
