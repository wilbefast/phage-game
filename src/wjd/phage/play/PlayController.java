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
package wjd.phage.play;

import wjd.amb.control.EUpdateResult;
import wjd.amb.control.IInput;
import wjd.amb.view.Colour;
import wjd.amb.view.ICanvas;
import wjd.math.Rect;
import wjd.phage.level.LevelController;
import wjd.phage.level.LevelScene;
import wjd.phage.level.Tile;

/**
 *
 * @author wdyce
 * @since Nov 1, 2012
 */
public class PlayController extends LevelController
{
  /* ATTRIBUTES */
  private Rect selection_box = new Rect(0,0,32,32);
  private boolean selecting = false;
  
  /* METHODS */

  // constructors
  public PlayController(LevelScene level)
  {
    super(level);
  }
  
  /* OVERRIDES -- CONTROLLER */
  
  @Override
  public EUpdateResult processInput(IInput input)
  {
    // stretch selection box
    if(input.isMouseClicking(IInput.EMouseButton.LEFT))
    {
      selection_box.endpos(input.getMousePosition());
    }
    
    // all clear
    return EUpdateResult.CONTINUE;
  }
  
  @Override
  public EUpdateResult processMouseClick(IInput.MouseClick event)
  {
    Tile target = level.perspectiveToTile(event.input.getMousePosition());  
    if(event.pressed)
    {
      selection_box.pos(event.input.getMousePosition());
    }
    
    selection_box.h = selection_box.w = 0;
    
    // all clear
    return EUpdateResult.CONTINUE;
  }
  
  
  @Override
  public void render(ICanvas canvas)
  {
    canvas.setLineWidth(2.0f);
    canvas.setColour(Colour.TEAL);
    canvas.box(selection_box, false);
  }
}
