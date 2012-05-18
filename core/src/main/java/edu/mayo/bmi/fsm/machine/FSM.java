package edu.mayo.bmi.fsm.machine;

import java.util.List;
import java.util.Set;

/**
 * Any finate state machine defined must implement this interface
 * @author M039575
 *
 */

public interface FSM
{
  public Set execute(List data) throws Exception;
}
