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
import wjd.amb.view.Colour;
import wjd.amb.view.ICanvas;
import wjd.math.Rect;
import wjd.math.V2;
import wjd.phage.level.LevelController;
import wjd.phage.level.LevelScene;
import wjd.util.ObjectCycle;

/**
 *
 * @author wdyce
 * @since Nov 1, 2012
 */
public class EditorController extends LevelController
{
  /* CONSTANTS */
  private static final Rect GUI_BOX = new Rect(0, 0, 640, 32);
  private static final String gui_txt[] =
  {
    "Save: CTRL", 
    "Load: ALT", 
    "Change Brush: SHIFT"
  };
  private static final V2 gui_pos[] = 
  {
    new V2(32, 8), new V2(128, 8), new V2(224, 8)
  };

  /* ATTRIBUTES */
  private final ObjectCycle<ABrush> brushes;
  
  /* METHODS */

  // constructors
  public EditorController(LevelScene level)
  {
    super(level);
    // create brush cycler
    ABrush brush_array[] = { new TerrainBrush(), new UnitBrush() };
    brushes = new ObjectCycle<ABrush>(brush_array);
  }
  
  /* OVERRIDES -- CONTROLLER */
  
  @Override
  public void render(ICanvas canvas)
  {
    // draw gui
    canvas.setColour(Colour.YELLOW);
    canvas.box(GUI_BOX, true);
    canvas.setColour(Colour.BLACK);
    for(int i = 0; i < gui_txt.length; i++)
      canvas.text(gui_txt[i], gui_pos[i]);
    
    // draw brush paint
    canvas.setCameraActive(true);
    brushes.current().render(canvas);
  }
  
  @Override
  public EUpdateResult processMouseClick(IInput.MouseClick event)
  {
    // change the "paint" of the current brush
    if(event.pressed && event.button == IInput.EMouseButton.RIGHT)
      brushes.current().changeColour();
    
    return EUpdateResult.CONTINUE;
  }
  
  @Override
  public EUpdateResult processKeyPress(IInput.KeyPress event)
  {
    // default interactions
    EUpdateResult result = super.processKeyPress(event);
    if(result != EUpdateResult.CONTINUE)
      return result;
    
    // change brush
    if(event.pressed)
    {
      if(event.key != null) switch(event.key)
      {
        case L_SHIFT:
        case R_SHIFT:
          // change brush on SHIFT
          brushes.next();
        break;
      }
    }
  
    // all clear
    return EUpdateResult.CONTINUE;
  }
  
  @Override
  public EUpdateResult processInput(IInput input)
  {
    // default input processing
    EUpdateResult result = super.processInput(input);
    if(result != EUpdateResult.CONTINUE)
      return result;
    
    // reset brush position
    brushes.current().setSize(1/level.getCamera().getZoom()); // size first!
    brushes.current().setPosition(level.getCamera().getGlobal(input.getMousePosition()));
    
    // "paint" using the current brush
    if(input.isMouseClicking(IInput.EMouseButton.LEFT))
        brushes.current().paint(level.tilegrid);

    return EUpdateResult.CONTINUE;
  }
}
