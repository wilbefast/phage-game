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

import java.util.LinkedList;
import java.util.List;
import wjd.amb.control.EUpdateResult;
import wjd.amb.control.IInput;
import wjd.amb.view.Colour;
import wjd.amb.view.ICanvas;
import wjd.math.Rect;
import wjd.math.V2;
import wjd.phage.level.LevelController;
import wjd.phage.level.LevelScene;
import wjd.phage.level.Tile;
import wjd.phage.level.TileGrid;
import wjd.phage.level.Unit;

/**
 *
 * @author wdyce
 * @since Nov 1, 2012
 */
public class PlayController extends LevelController
{
  /* ATTRIBUTES */
  private Rect selection_box = new Rect();
  private List<Unit> selected_units = new LinkedList<Unit>();
  
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
    // default input processing
    EUpdateResult result = super.processInput(input);
    if(result != EUpdateResult.CONTINUE)
      return result;
    
    // stretch selection box
    if(input.isMouseClicking(IInput.EMouseButton.LEFT))
      selection_box.endpos(input.getMousePosition());
    
    // all clear
    return EUpdateResult.CONTINUE;
  }
  
  @Override
  public EUpdateResult processMouseClick(IInput.MouseClick event)
  {
    // pressed?
    if(event.pressed)
      
      selection_box.pos(event.input.getMousePosition());
    
    // released?
    else 
    {
      // deselect previous units
      deselectAll();
      
      // calculate selection area
      selection_box.makePositive();
      Rect global_selection = level.getCamera().getGlobal(selection_box);
      
      // drag-selection?
      if(selection_box.w != 0 && selection_box.h != 0)
      {
        TileGrid selection = level.tilegrid.createSubGrid(global_selection);
        if(selection != null) for(Tile t : selection)
          select(t);
      }
      // click-unclick?
      else
        select(level.tilegrid.getTile(global_selection.pos()));
    }
    
    // close selection box
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
  
  /* SUBROUTINES */
  
  private void deselectAll()
  {
    for(Unit u : selected_units)
      u.setSelected(false);
    selected_units.clear();
  }
  
  private void select(Tile t)
  {
    if(t == null)
      return;
      
    Unit u = t.getUnit();
    if (u != null)
    {
      u.setSelected(true);
      selected_units.add(u);
    }
  }
}
