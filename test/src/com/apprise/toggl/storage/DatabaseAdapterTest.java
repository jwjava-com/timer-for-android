import com.apprise.toggl.storage.DatabaseAdapter;
import com.apprise.toggl.storage.DatabaseAdapter.Users;
import com.apprise.toggl.storage.DatabaseAdapter.Workspaces;
import com.apprise.toggl.storage.models.User;
import com.apprise.toggl.storage.models.Workspace;

import android.database.Cursor;
import android.test.AndroidTestCase;


public class DatabaseAdapterTest extends AndroidTestCase {
  
  private final String TEST_DATABASE_NAME = "toggl_test.db";
  private DatabaseAdapter dbAdapter;  

  @Override
  protected void setUp() throws Exception {
    dbAdapter = new DatabaseAdapter(getContext());
    dbAdapter.setDatabaseName(TEST_DATABASE_NAME);
    dbAdapter.open();
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    dbAdapter.close();
    getContext().deleteDatabase(TEST_DATABASE_NAME);
    super.tearDown();
  }
  
  public void testCreateUser() {
    User userContents = new User();
    userContents.api_token = "tokitoki123";
    userContents.email = "bat@man.com";
    User createdUser = dbAdapter.createUser(userContents);
    assertNotNull(createdUser);
    assertEquals("tokitoki123", createdUser.api_token);
    assertEquals("bat@man.com", createdUser.email);
  }
  
  public void testFindUser() {
    User user = dbAdapter.createUser(new User());
    user.email = "cat@woman.com";
    dbAdapter.updateUser(user);
    User foundUser = dbAdapter.findUser(user._id);
    
    assertNotNull(foundUser);
    assertEquals(user.id, foundUser.id);
    assertEquals("cat@woman.com", foundUser.email);
  }
  
  public void testFindUserByRemoteId() {
    User user = dbAdapter.createUser(new User());
    user.id = 345;
    user.email = "aqua@man.com";
    dbAdapter.updateUser(user);
    
    User foundUser = dbAdapter.findUserByRemoteId(345);
    assertNotNull(foundUser);
    assertEquals(345, foundUser.id);
    assertEquals("aqua@man.com", foundUser.email);
    assertEquals(user.id, foundUser.id);
  }
  
  public void testUpdateUser() {
    User user = dbAdapter.createUser(new User());
    assertEquals(0l, user.id);
    assertEquals(null, user.api_token);
    
    user.beginning_of_week = 2;
    user.date_format = "%m/%d/%Y";
    user.default_workspace_id = 555;
    user.fullname = "Robin Hood Loxley";
    user.id = 123;
    user.jquery_date_format = "m/d/Y";
    user.jquery_timeofday_format = "H:i";
    user.language = "en_GB";
    user.new_tasks_start_automatically = false;
    user.task_retention_days = 5;
    user.timeofday_format = "%H:%M";
    user.email = "robin@hood.com";
    user.api_token = "jibberish";
    dbAdapter.updateUser(user);

    User foundUser = dbAdapter.findUser(user._id);
    assertEquals(2, foundUser.beginning_of_week);
    assertEquals("%m/%d/%Y", foundUser.date_format);
    assertEquals(555, foundUser.default_workspace_id);
    assertEquals("Robin Hood Loxley", foundUser.fullname);
    assertEquals(123, foundUser.id);
    assertEquals("m/d/Y", foundUser.jquery_date_format);
    assertEquals("H:i", foundUser.jquery_timeofday_format);
    assertEquals("en_GB", foundUser.language);
    assertEquals(false, foundUser.new_tasks_start_automatically);
    assertEquals(5, foundUser.task_retention_days);
    assertEquals("%H:%M", foundUser.timeofday_format);
    assertEquals("robin@hood.com", foundUser.email);
    assertEquals("jibberish", foundUser.api_token);
  }
  
  public void testFindAllUsers() {
    User user1 = dbAdapter.createUser(new User());
    User user2 = dbAdapter.createUser(new User());
    
    Cursor allUsers = dbAdapter.findAllUsers();
    assertNotNull(allUsers);
    assertEquals(2, allUsers.getCount());
    allUsers.moveToFirst();
    assertEquals(user1._id, allUsers.getLong(allUsers.getColumnIndex(Users._ID)));
    allUsers.moveToNext();
    assertEquals(user2._id, allUsers.getLong(allUsers.getColumnIndex(Users._ID)));
  }
  
  public void testDeleteUser() {
    User user = dbAdapter.createUser(new User());
    int deletedId = dbAdapter.deleteUser(user._id);
    
    assertEquals(user._id, deletedId);
    assertNull(dbAdapter.findUser(user._id));
  }
  
  public void testCreateWorkspace() {
    Workspace workspaceContents = new Workspace();
    workspaceContents.name = "work@space.com";
    Workspace createdWorkspace = dbAdapter.createWorkspace(workspaceContents);
    
    assertNotNull(createdWorkspace);
    assertEquals("work@space.com", createdWorkspace.name);
  }
  
  public void testFindWorkspace() {
    Workspace workspace = new Workspace();
    workspace.name = "jaha@baha.fa";
    Workspace createdWorkspace = dbAdapter.createWorkspace(workspace);

    Workspace foundWorkspace = dbAdapter.findWorkspace(createdWorkspace._id);
    assertNotNull(foundWorkspace);
    assertEquals(createdWorkspace._id, foundWorkspace._id);
    assertEquals("jaha@baha.fa", foundWorkspace.name);
  }
  
  public void testFindWorkspaceByRemoteId() {
    Workspace workspace = new Workspace();
    workspace.name = "jaha@baha.fa";
    Workspace createdWorkspace = dbAdapter.createWorkspace(workspace);
    
    Workspace foundWorkspace = dbAdapter.findWorkspaceByRemoteId(createdWorkspace.id);
    assertNotNull(foundWorkspace);
    assertEquals(createdWorkspace._id, foundWorkspace._id);
    assertEquals("jaha@baha.fa", foundWorkspace.name);
  }
  
  public void testFindAllWorkspaces() {
    Workspace workspace1 = dbAdapter.createWorkspace(new Workspace());
    Workspace workspace2 = dbAdapter.createWorkspace(new Workspace());
    
    Cursor allWorkspaces = dbAdapter.findAllWorkspaces();
    assertNotNull(allWorkspaces);
    assertEquals(2, allWorkspaces.getCount());
    allWorkspaces.moveToFirst();
    assertEquals(workspace1._id, allWorkspaces.getLong(allWorkspaces.getColumnIndex(Workspaces._ID)));
    allWorkspaces.moveToNext();
    assertEquals(workspace2._id, allWorkspaces.getLong(allWorkspaces.getColumnIndex(Workspaces._ID)));
  }
  
  public void testUpdateWorkspace() {
    Workspace workspace = dbAdapter.createWorkspace(new Workspace());
    assertEquals(0l, workspace.id);
    assertEquals(null, workspace.name);
    
    workspace.id = 2;
    workspace.name = "asd@asd.ee";
    dbAdapter.updateWorkspace(workspace);
    
    Workspace foundWorkspace = dbAdapter.findWorkspace(workspace._id);
    assertEquals(2, foundWorkspace.id);
    assertEquals("asd@asd.ee", foundWorkspace.name);
  }  
  
  public void testDeleteWorkspace() {
    Workspace workspace = dbAdapter.createWorkspace(new Workspace());
    int deletedId = dbAdapter.deleteWorkspace(workspace._id);
    
    assertEquals(workspace._id, deletedId);
    assertNull(dbAdapter.findWorkspace(workspace._id));
  }  
}


