package com.feed_the_beast.ftbquests.quest.tasks;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.ProgressingQuestObject;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.tile.TileScreenCore;
import com.feed_the_beast.ftbquests.tile.TileScreenPart;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public abstract class QuestTask extends ProgressingQuestObject
{
	public final Quest quest;
	public int index;

	public QuestTask(Quest q)
	{
		quest = q;
		index = -1;
	}

	public abstract QuestTaskData createData(IProgressData data);

	@Override
	public final QuestFile getQuestFile()
	{
		return quest.chapter.file;
	}

	@Override
	public final QuestObjectType getObjectType()
	{
		return QuestObjectType.TASK;
	}

	@Override
	public final String getID()
	{
		return quest.chapter.id + ':' + quest.id + ':' + id;
	}

	@Override
	public final long getProgress(IProgressData data)
	{
		return data.getQuestTaskData(this).getProgress();
	}

	@Override
	public final double getRelativeProgress(IProgressData data)
	{
		return data.getQuestTaskData(this).getRelativeProgress();
	}

	@Override
	public final boolean isComplete(IProgressData data)
	{
		long max = getMaxProgress();
		return max > 0L && getProgress(data) >= max;
	}

	@Override
	public long getMaxProgress()
	{
		return 1;
	}

	public String getMaxProgressString()
	{
		return Long.toString(getMaxProgress());
	}

	@Override
	public final void resetProgress(IProgressData data)
	{
		QuestTaskData taskData = data.getQuestTaskData(this);
		taskData.resetProgress();
		taskData.isComplete = false;
		data.syncTask(taskData);
	}

	@Override
	public void completeInstantly(IProgressData data)
	{
		QuestTaskData taskData = data.getQuestTaskData(this);
		taskData.completeInstantly();
		data.syncTask(taskData);
	}

	@Override
	public void deleteSelf()
	{
		quest.tasks.remove(this);

		for (IProgressData data : quest.chapter.file.getAllData())
		{
			data.removeTask(this);
		}

		super.deleteSelf();
	}

	@Override
	public void deleteChildren()
	{
	}

	@Override
	public void onCreated()
	{
		for (IProgressData data : ServerQuestFile.INSTANCE.getAllData())
		{
			data.createTaskData(this);
		}
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		return QuestTaskType.getType(getClass()).getDisplayName();
	}

	public Class<? extends TileScreenCore> getScreenCoreClass()
	{
		return TileScreenCore.class;
	}

	public Class<? extends TileScreenPart> getScreenPartClass()
	{
		return TileScreenPart.class;
	}

	public TileScreenCore createScreenCore(World world)
	{
		return new TileScreenCore();
	}

	public TileScreenPart createScreenPart(World world)
	{
		return new TileScreenPart();
	}

	@SideOnly(Side.CLIENT)
	public void drawGUI(@Nullable QuestTaskData data, int x, int y, int w, int h)
	{
		getIcon().draw(x, y, w, h);
	}

	@SideOnly(Side.CLIENT)
	public void drawScreen(@Nullable QuestTaskData data)
	{
		getIcon().draw3D(Icon.EMPTY);
	}

	public boolean canInsertItem()
	{
		return false;
	}

	@Override
	public final void getExtraConfig(ConfigGroup config)
	{
		super.getExtraConfig(config);
	}

	@Override
	public Icon getIcon()
	{
		Icon icon = super.getIcon();
		return icon.isEmpty() ? GuiIcons.DICE : icon;
	}
}