package com.iver.utiles.swing.threads;

import org.gvsig.topology.Messages;


/**
 * Generic task to avoid to rewrite many tasks each time,
 * with their own cancel monitors.
 * 
 * When a time consuming bussines logic process is launched, (for example
 * FLyrVect.queryRect) internally it reports progress calling CancellableMonitorable.reportStep.
 * 
 * If this process runs in background, it is wrapped by a IMonitorableTask. 
 * Swing progress components talks with this IMonitorableTask with its methods
 * getNote(), getCurrentStep(), etc.
 * 
 * This class mixes both responsabilities to easy the work of ITask developers.
 
  Example:
  
  
   public class TopologyValidationTask extends CancellableProgressTask{
   		Topology topology;
   		
   		static final String NOTE_PREFIX = Messages.getText("VALIDANDO_REGLA);
   		
   		public TopologyValidationTask(Topology topology){
   			this.topology = topology;
   			super.statusMessage = Messages.getText("VALIDANDO_TOPOLOGIA");
   			super.currentNote = NOTE_PREFIX + 
   						Messages.getText(topology.getRule(0).getName());
   		}
   	   		
   		public void run() throws Exception{
   			topology.validate(this);
   		}
   		
   }
     
   class Topology{
   		void validate(CancellableProgressTask monitor){
   			Iterator<TopologyRule> rules = topology.getRulesIterator();
   			while(rules.hasNext()){
   				TopologyRule rule = rules.next();
   				if(monitor != rule){
   					monitor.setNote(Messages.getText("VALIDANDO_REGLA")+" "+
   						Messages.getText(rule.getName());
   					monitor.reportStep();
   				}
   				rule.check();
   			
   			}//while
   		}//validate
   		
   }//Topology
   
   
   
   PluginServices.run(new TopologyValidationTask(topology));
   
 * @author azabala
 *
 */

//TODO Esta clase debería estar en el proyecto libIverUtiles
public abstract class CancellableProgressTask 
	extends DefaultCancellableMonitorable implements IMonitorableTask{

	/**
	 * Primary message that describes the logic of the task this IMonitorableTask
	 * is doing.
	 */
	protected String statusMessage;
	
	/**
	 * Secondary message that describes the current step that
	 * the task is doing.
	 */
	protected String currentNote;
	
	/**
	 * Marks if this process is finished.
	 */
	protected boolean finished;
	
	/**
	 * String connector to link the currentStep with the total number of steps
	 * (spanish example: 1 de 100)
	 * (english example: 1 of 100)
	 */
	public final String OF_CONNECTOR = Messages.getText("de");//FIXME Use Messages here.

	/**
	 * IMonitorableTask nomenclature is getFinishStep, 
	 * that is a wrapper to DefaultCancellableMonitorable
	 */
	public int getFinishStep() {
		return getFinalStep();
	}
	
	/**
	 * IMonitorableTask nomenclature is isDefined(),
	 * that is a wrapper to DefaultCancellableMonitorable isDeterminatedProcess
	 * method.
	 */
	public boolean isDefined() {
		return isDeterminatedProcess();
	}

	/**
	 * By default, getNote() shows the progress of the task
	 */
	public String getNote() {
		return currentNote + ": " + getCurrentStep() + " " + OF_CONNECTOR + " "
				+ getFinishStep();
	}
	
	public void setNote(String note){
		this.currentNote = note;
	}

	
	public String getStatusMessage() {
		return statusMessage;
	}
	
	public void setStatusMessage(String statusMessage){
		this.statusMessage = statusMessage;
	}

	public void cancel() {
		setCanceled(true);
	}

	public abstract void run() throws Exception;
	
	public abstract void finished();

	public boolean isFinished() {
		return finished;
	}
}

