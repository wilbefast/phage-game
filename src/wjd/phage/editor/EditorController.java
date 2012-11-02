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
package wjd.phage.editor;

import wjd.amb.control.EUpdateResult;
import wjd.amb.control.IInput;
import wjd.phage.level.LevelController;
import wjd.phage.level.LevelScene;

/**
 *
 * @author wdyce
 * @since Nov 1, 2012
 */
public class EditorController extends LevelController
{
  /* ATTRIBUTES */
  IBrush brush;
  
  /* METHODS */

  // constructors
  public EditorController(LevelScene level)
  {
    super(level);
    
    brush = new TerrainBrush();
  }

  /* OVERRIDES -- CONTROLLER */
  
  @Override
  public EUpdateResult processMouseClick(IInput.MouseClick event)
  {
    // change the "paint" of the current brush
    if(event.state && event.button == IInput.EMouseButton.RIGHT)
      brush.changeColour();
    
    return EUpdateResult.CONTINUE;
  }
  
  @Override
  public EUpdateResult processInput(IInput input)
  {
    // "paint" using the current brush
    if(input.isMouseClicking(IInput.EMouseButton.LEFT))
      brush.paint(level.perspectiveToTile(input.getMousePosition()));

    return EUpdateResult.CONTINUE;
  }
}