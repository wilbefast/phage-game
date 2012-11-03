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

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import wjd.amb.control.EUpdateResult;
import wjd.amb.control.IInput;
import wjd.amb.view.Colour;
import wjd.amb.view.ICanvas;
import wjd.math.Rect;
import wjd.math.V2;
import wjd.phage.level.LevelController;
import wjd.phage.level.LevelScene;
import wjd.phage.level.Tile;

/**
 *
 * @author wdyce
 * @since Nov 1, 2012
 */
public class EditorController extends LevelController
{
  /* CONSTANTS */
  private static final JFileChooser fileChooser = new JFileChooser();
  private static final FileFilter levelFilter = new FileFilter()
  {
    @Override
    public boolean accept(File file)
    {
      // allow directories
      if(file == null || file.isDirectory())
        return true;
      
      String filename = file.getName(),
             extension = filename.substring(filename.lastIndexOf('.')+1);
      return (extension.equals("lvl"));
    }

    @Override
    public String getDescription()
    {
      return "Game level (LVL) files";
    }
  };
  private static final ABrush[] BRUSHES = 
  { 
    new TerrainBrush(), 
    new UnitBrush() 
  };
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
  
    
  static
  {
    fileChooser.setAcceptAllFileFilterUsed(false);
    fileChooser.addChoosableFileFilter(levelFilter);
  }
 
  /* ATTRIBUTES */
  private int brush_i = 0;
  private Tile previous_target = null;
  
  /* METHODS */

  // constructors
  public EditorController(LevelScene level)
  {
    super(level);
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
    BRUSHES[brush_i].render(canvas);
  }
  
  @Override
  public EUpdateResult processMouseClick(IInput.MouseClick event)
  {
    // change the "paint" of the current brush
    if(event.state && event.button == IInput.EMouseButton.RIGHT)
    {
      BRUSHES[brush_i].changeColour();
      previous_target = null;
    }
    
    return EUpdateResult.CONTINUE;
  }
  
  @Override
  public EUpdateResult processKeyPress(IInput.KeyPress event)
  {
    // default interactions
    EUpdateResult result = super.processKeyPress(event);
    if(result != EUpdateResult.CONTINUE)
      return result;
   
    if(event.state)
    {
      if(event.key != null) switch(event.key)
      {
        case L_CTRL:
        case R_CTRL:
          // save on CONTROL
          if(fileChooser.showSaveDialog(fileChooser) == JFileChooser.APPROVE_OPTION)
            level.save(fileChooser.getSelectedFile());
        break;

        case L_ALT:
        case R_ALT:
          // load on ALT
          if(fileChooser.showOpenDialog(fileChooser) == JFileChooser.APPROVE_OPTION)
            level.load(fileChooser.getSelectedFile());
        break;
          
        case L_SHIFT:
        case R_SHIFT:
          // change brush on SHIFT
          brush_i = (brush_i + 1)%BRUSHES.length;
          previous_target = null;
        break;
      }
    }
    
    return EUpdateResult.CONTINUE;
  }
  
  private static V2 pos = new V2();
  @Override
  public EUpdateResult processInput(IInput input)
  {
    // default input processing
    EUpdateResult result = super.processInput(input);
    if(result != EUpdateResult.CONTINUE)
      return result;
    
    // reset brush position
    BRUSHES[brush_i].setPosition(input.getMousePosition());
    int brush_size = (int) (1/level.getCamera().getZoom()),
        brush_hsize = brush_size/2;
    
    // "paint" using the current brush
    if(input.isMouseClicking(IInput.EMouseButton.LEFT))
    {
      Tile target = level.perspectiveToTile(input.getMousePosition());
      if(target != null && (previous_target == null || previous_target != target))
      {
        for(pos.y = target.position.y-brush_hsize; 
            pos.y < target.position.y+brush_hsize+1; pos.y++)
        for(pos.x = target.position.x-brush_hsize; 
            pos.x < target.position.x+brush_hsize+1; pos.x++)
        if(level.validGridPos(pos))
          BRUSHES[brush_i].paint(target.tilegrid[(int)pos.y][(int)pos.x]);
        previous_target = target;
      }
    }

    return EUpdateResult.CONTINUE;
  }
}
