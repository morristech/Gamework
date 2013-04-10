package cz.robyer.gamework.scenario;

/**
 * Base identificable object.
 * @author Robert P�sel
 */
public abstract class IdentificableObject extends BaseObject {
	protected final String id;
	protected final String TAG;
	
	public IdentificableObject(String id) {
		super();
		this.id = id;
		this.TAG = String.format("%s (%s)", getClass().getSimpleName(), id);
	}

	public String getId() {
		return id;
	}
	
}