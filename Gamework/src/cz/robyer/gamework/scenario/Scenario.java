package cz.robyer.gamework.scenario;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import cz.robyer.gamework.game.GameHandler;
import cz.robyer.gamework.scenario.area.Area;
import cz.robyer.gamework.scenario.helper.EventHookable;
import cz.robyer.gamework.scenario.helper.ScannerHookable;
import cz.robyer.gamework.scenario.helper.TimeHookable;
import cz.robyer.gamework.scenario.hook.Hook;
import cz.robyer.gamework.scenario.message.Message;
import cz.robyer.gamework.scenario.reaction.Reaction;
import cz.robyer.gamework.scenario.variable.Variable;

/**
 * Represents definition of a whole game - game objects and behavior.
 * @author Robert P�sel
 */
public class Scenario {
	private static final String TAG = Scenario.class.getSimpleName();
	
	protected final Context context;
	protected final ScenarioInfo info;
	protected GameHandler handler;
	
	protected final Map<String, Area> areas = new HashMap<String, Area>();
	protected final Map<String, Variable> variables = new HashMap<String, Variable>();
	protected final Map<String, Reaction> reactions = new HashMap<String, Reaction>();
	protected final Map<String, Message> messages = new HashMap<String, Message>();
	protected final List<Hook> hooks = new ArrayList<Hook>();
	
	// TODO: implement onLoadComplete listener and wait for its loading before starting game
	// http://stackoverflow.com/questions/14782579/waiting-for-soundpool-to-load-before-moving-on-with-application
	// http://stackoverflow.com/questions/14300293/soundpool-loading-never-ends
	// http://stackoverflow.com/questions/5202510/soundpool-sample-not-ready
	protected final SoundPool soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
	
	// Helpers providing hookable interface for special events
	protected final TimeHookable timeHookable = new TimeHookable(this);
	protected final ScannerHookable scannerHookable = new ScannerHookable(this);
	protected final EventHookable eventHookable = new EventHookable(this);
	
	/**
	 * Class constructor.
	 * @param context - application context
	 * @param info - scenario info
	 */
	public Scenario(Context context, ScenarioInfo info) {
		this.context = context;
		this.info = info;
	}
	
	/**
	 * Class constructor
	 * @param context - application context
	 * @param handler - game handler
	 * @param info - scenario info
	 */
	public Scenario(Context context, GameHandler handler, ScenarioInfo info) {
		this(context, info);
		setHandler(handler);
	}

	public Context getContext() {
		return context;
	}
	
	public GameHandler getHandler() {
		return handler;
	}
	
	public void setHandler(GameHandler handler) {
		this.handler = handler;
	}

	public SoundPool getSoundPool() {
		return soundPool;
	}
	
	public void addArea(String id, Area area) {
		if (area == null) {
			Log.w(TAG, "addArea() with null area");
			return;
		}
		
		if (areas.containsKey(id))
			Log.w(TAG, "Duplicit definition of area id='" + id + "'");
		
		areas.put(id, area);
		area.setScenario(this);
	}
	
	public void addArea(Area area) {
		addArea(area.getId(), area);
	}
	
	public Area getArea(String id) {
		return areas.get(id);
	}
	
	public Map<String, Area> getAreas() {
		return areas;
	}
	
	public void addVariable(String id, Variable variable) {
		if (variable == null) {
			Log.w(TAG, "addVariable() with null variable");
			return;
		}
		
		if (variables.containsKey(id))
			Log.w(TAG, "Duplicit definition of variable id='" + id + "'");
		
		variables.put(id, variable);
		variable.setScenario(this);
	}
	
	public void addVariable(Variable variable) {
		addVariable(variable.getId(), variable);
	}
	
	public Variable getVariable(String id) {
		return variables.get(id);
	}

	public void addReaction(String id, Reaction reaction) {
		if (reaction == null) {
			Log.w(TAG, "addReaction() with null reaction");
			return;
		}
		
		if (reactions.containsKey(id))
			Log.w(TAG, "Duplicit definition of reaction id='" + id + "'");
		
		reactions.put(id, reaction);
		reaction.setScenario(this);
	}
	
	public void addReaction(Reaction reaction) {
		addReaction(reaction.getId(), reaction);
	}
	
	public Reaction getReaction(String id) {
		return reactions.get(id);
	}
	
	public void addMessage(String id, Message message) {
		if (message == null) {
			Log.w(TAG, "addMessage() with null message");
			return;
		}
		
		if (messages.containsKey(id))
			Log.w(TAG, "Duplicit definition of message id='" + id + "'");
		
		messages.put(id, message);
		message.setScenario(this);
	}
	
	public void addMessage(Message message) {
		addMessage(message.getId(), message);
	}
	
	public Message getMessage(String id) {
		return messages.get(id);
	}
	
	/**
	 * Returns all visible (received and not deleted) messages with specified tag.
	 * @param tag of message, use empty string for messages without tag or null for all messages
	 * @return list of messages
	 */
	public List<Message> getVisibleMessages(String tag) {
		List<Message> list = new ArrayList<Message>();
		for (Message m : messages.values()) {
			if (m.isVisible() && (tag == null || tag.equalsIgnoreCase(m.getTag())))
				list.add(m);
		}
		return list;
	}

	public void addHook(Hook hook) {
		if (hook == null) {
			Log.w(TAG, "addHook() with null hook");
			return;
		}
		
		hooks.add(hook);
		hook.setScenario(this);
	}

	public ScenarioInfo getInfo() {
		return info;
	}

	public void onTimeUpdate(long time) {
		timeHookable.updateTime(time);
	}

	public void onLocationUpdate(double lat, double lon) {
		for (Area a : areas.values()) {
			a.updateLocation(lat, lon);
		}
	}
	
	public void onCustomEvent(Object data) {
		eventHookable.update(data);
	}
	
	/**
	 * Called after sucessfuly scanned code.
	 * @param data - scanned value from code
	 */
	public void onScanned(Object data) {
		scannerHookable.update(data);
	}

	private void initializeHooks() {
		Log.i(TAG, "Initializing hooks");
		for (Hook hook : hooks) {
			HookableObject hookable = null;
			String type = null;
			
			switch (hook.getType()) {
			case AREA:
			case AREA_ENTER:
			case AREA_LEAVE:
				hookable = areas.get(hook.getValue());
				type = "Area";
				break;
			case VARIABLE:
				hookable = variables.get(hook.getValue());
				type = "Variable";
				break;
			case TIME:
				hookable = timeHookable;
				type = "Time";
				break;
			case SCANNER:
				hookable = scannerHookable;
				type = "Scanner";
				break;
			case EVENT:
				hookable = eventHookable;
				type = "Event";
				break;
			}

			if (hookable != null) {
				hookable.addHook(hook);
			} else {
				Log.e(TAG, String.format("Hook can't be attached to %s '%s'", type, hook.getValue()));
			}
		}
	}
	
	/**
	 * Called after whole scenario was loaded (all game items are present).
	 */
	public void onLoaded() {
		Log.i(TAG, "Scenario objects was loaded");
		
		initializeHooks();
		
		boolean ok = true;
		
		for (BaseObject obj : areas.values())
			if (!obj.onScenarioLoaded())
				ok = false;
		
		for (BaseObject obj : reactions.values())
			if (!obj.onScenarioLoaded())
				ok = false;
		
		for (BaseObject obj : variables.values())
			if (!obj.onScenarioLoaded())
				ok = false;
		
		for (BaseObject obj : hooks)
			if (!obj.onScenarioLoaded())
				ok = false;
		
		if (!ok)
			Log.e(TAG, "onScenarioLoaded() finished with errors");
		else
			Log.i(TAG, "onScenarioLoaded() finished without errors");
	}

}
