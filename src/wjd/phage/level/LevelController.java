/*
 Copyright (C) 2012 William James Dyce

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package wjd.phage.level;

import wjd.amb.control.Controller;
import wjd.amb.control.EUpdateResult;
import wjd.amb.control.IInput;
import wjd.amb.view.ICanvas;
import wjd.amb.view.IVisible;

/**
 *
 * @author wdyce
 * @since Nov 2, 2012
 */
public abstract class LevelController extends Controller implements IVisible
{
  /* ATTRIBUTES */
  protected LevelScene level;
  
  /* METHODS */
  
  // constructors
  public LevelController(LevelScene level)
  {
    this.level = level;
  }
  
  /* IMPLEMENTS -- IVISIBLE */
  
  @Override
  public void render(ICanvas canvas)
  {
    // overridden if needed
  }
  
  /* OVERRIDES -- CONTROLLER */
  
  @Override
  public EUpdateResult processInput(IInput input)
  {
    // control camera
    level.getCamera().processInput(input);
    
    // all clear
    return EUpdateResult.CONTINUE;
  }
  
  @Override
  public EUpdateResult processKeyPress(IInput.KeyPress event)
  {
    // exit on key press
    if(event.key == IInput.EKeyCode.ESC)
      return EUpdateResult.STOP;
    
    // all clear
    return EUpdateResult.CONTINUE;
  }
}
